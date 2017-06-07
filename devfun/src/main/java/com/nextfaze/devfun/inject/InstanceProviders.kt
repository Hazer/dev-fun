package com.nextfaze.devfun.inject

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.support.annotation.RestrictTo
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.view.View
import android.view.ViewGroup
import com.nextfaze.devfun.internal.*
import java.util.ArrayDeque
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.isAccessible

private inline fun <reified T : Any> KClass<*>.isSubclassOf() = T::class.java.isAssignableFrom(this.java)
private fun <T : Any> KClass<*>.isSubclassOf(clazz: KClass<T>) = clazz.java.isAssignableFrom(this.java)

/**
 * Instance provider that delegates to other providers.
 *
 * Checks in reverse order of added.
 * i.e. most recently added is checked first
 *
 * @internal Visible for testing - use at your own risk.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class CompositeInstanceProvider : RequiringInstanceProvider {
    private val log = logger()
    private val instanceProviders = ArrayDeque<InstanceProvider>()

    internal fun clear() = instanceProviders.clear()

    override fun <T : Any> get(clazz: KClass<out T>): T {
        instanceProviders.descendingIterator().forEach {
            log.t { "Try-get instanceOf $clazz from $it" }
            it[clazz]?.let {
                log.t { "> Got $it" }
                return it
            }
        }
        if (clazz.isSubclassOf<InstanceProvider>()) {
            instanceProviders.firstOrNull { it::class.isSubclassOf(clazz) }?.let {
                @Suppress("UNCHECKED_CAST")
                return it as T
            }
        }
        throw ClassInstanceNotFoundException(clazz)
    }

    operator fun plusAssign(instanceProvider: InstanceProvider) {
        instanceProviders += instanceProvider
    }

    operator fun minusAssign(instanceProvider: InstanceProvider) {
        instanceProviders -= instanceProvider
    }
}

/**
 * Handles Kotlin `object` types.
 *
 * @internal Visible for testing - use at your own risk.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class KObjectInstanceProvider : InstanceProvider {
    override fun <T : Any> get(clazz: KClass<out T>): T? {
        if (clazz.visibility != KVisibility.PRIVATE) {
            return clazz.objectInstance?.let { return it }
        } else {
            try {
                @Suppress("UNCHECKED_CAST")
                return clazz.java.getDeclaredField("INSTANCE").apply { isAccessible = true }.get(null) as T
            } catch (ignore: NoSuchFieldException) {
                return null
            }
        }
    }
}

/**
 * Provides objects via instance construction. Type must be annotated with [Constructable].
 *
 * Only supports objects with a single constructor. Constructor arguments will fetched using [root].
 *
 * @param rootInstanceProvider An instance provider used to fetch constructor args. If `null`,  then self (`this`) is used
 * @param requireConstructable Flag indicating if a type must be [Constructable] to be instantiable
 *
 * @internal Visible for testing - use at your own risk.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ConstructingInstanceProvider(rootInstanceProvider: InstanceProvider? = null,
                                   var requireConstructable: Boolean = true) : InstanceProvider {
    private val log = logger()
    private val root = rootInstanceProvider ?: this

    override fun <T : Any> get(clazz: KClass<out T>): T? {
        // Must be @Constructable
        if (requireConstructable && clazz.annotations.none { it is Constructable }) {
            return null
        }
        if (clazz.constructors.isEmpty()) {
            throw ClassInstanceNotFoundException("Could not get instance of @Constructable $clazz and no constructors found (is it an interface?)")
        }
        // Instantiate new instance
        if (clazz.constructors.size != 1) {
            throw ClassInstanceNotFoundException("Could not get instance of @Constructable $clazz and more than one constructor found")
        }
        log.t { "> Constructing new instance of $clazz" }
        val constructor = clazz.constructors.first().apply { isAccessible = true }
        return constructor.call(*(constructor.parameters.map { root[it.type.classifier as KClass<*>] }.toTypedArray()))
    }
}

internal class AndroidInstanceProvider(context: Context,
                                       private val activityProvider: ActivityProvider) : InstanceProvider {
    private val applicationContext = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(clazz: KClass<out T>): T? {
        // Activities
        val activity = activityProvider()
        if (activity != null && clazz.isSubclassOf<Activity>()) {
            return activity as T
        }

        // Application
        if (clazz.isSubclassOf<Application>()) {
            return applicationContext as T
        }

        // Context
        if (clazz.isSubclassOf<Context>()) {
            return (activity ?: applicationContext) as T
        }

        // Fragments
        if (activity is FragmentActivity && clazz.isSubclassOf<Fragment>()) {
            activity.supportFragmentManager.iterateChildren().forEach {
                // not sure why, but under some circumstances some of them are null...
                it ?: return@forEach

                if (it.isAdded && it::class.isSubclassOf(clazz)) {
                    @Suppress("UNCHECKED_CAST")
                    return it as T
                }
            }
        }

        // Views
        if (activity != null && clazz.isSubclassOf<View>()) {
            (activity.findViewById(android.R.id.content) as? ViewGroup)?.let { traverseViewHierarchy(it, clazz) }?.let { return it }
            if (activity is FragmentActivity) {
                activity.supportFragmentManager.iterateChildren().forEach {
                    (it?.view as? ViewGroup)?.let { traverseViewHierarchy(it, clazz) }?.let { return it }
                }
            }
        }

        return null
    }
}

//
// Fragment Traversal
//

private val peekChildFragmentManagerMethod = Fragment::class.java.getDeclaredMethod("peekChildFragmentManager").apply { isAccessible = true }
private val Fragment.hasChildFragmentManager get() = peekChildFragmentManagerMethod.invoke(this) != null

private fun FragmentManager.iterateChildren(): Iterator<Fragment?> {
    @SuppressLint("RestrictedApi")
    val fragments = this.fragments ?: return EmptyIterator()

    class FragmentManagerIterator : Iterator<Fragment?> {
        private val iterators = ArrayDeque<Iterator<Fragment?>>().apply { push(fragments.iterator()) }

        override fun hasNext(): Boolean {
            while (true) {
                val it = iterators.peek() ?: return false
                if (it.hasNext()) {
                    return true
                } else {
                    iterators.pop()
                }
            }
        }

        override fun next(): Fragment? {
            val fragment = iterators.peek().next()
            if (fragment != null && fragment.hasChildFragmentManager) {
                iterators.push(fragment.childFragmentManager.iterateChildren())
            }

            return fragment
        }
    }

    return FragmentManagerIterator()
}

private class EmptyIterator<out E> : Iterator<E> {
    override fun hasNext() = false
    override fun next() = throw NoSuchElementException()
}

//
// View Traversal
//

private inline fun ViewGroup.forEachChild(operation: (View) -> Unit): Unit {
    for (element in iterateChildren()) operation(element)
}

private fun ViewGroup.iterateChildren(): Iterator<View> {
    class ViewGroupIterator : Iterator<View> {
        private val childCount = this@iterateChildren.childCount
        private var current = 0

        override fun hasNext() = current < childCount
        override fun next() = this@iterateChildren.getChildAt(current++)
    }

    return ViewGroupIterator()
}

private fun <T : Any> traverseViewHierarchy(viewGroup: ViewGroup, clazz: KClass<out T>): T? {
    viewGroup.forEachChild {
        if (it::class.isSubclassOf(clazz)) {
            @Suppress("UNCHECKED_CAST")
            return it as T
        } else if (it is ViewGroup) {
            return traverseViewHierarchy(it, clazz)
        }
    }
    return null
}
