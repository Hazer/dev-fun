package com.nextfaze.devfun.inject

import com.nextfaze.devfun.core.FunctionTransformer
import kotlin.reflect.KClass

/**
 * Provides object instances for one or more types.
 *
 * A rudimentary form of dependency injection is used throughout all of DevFun (not just for user-code function
 * invocation, but also between modules, definition and item processing, and anywhere else an object of some type is
 * desired - in general nothing in DevFun is static (except for the occasional `object`, but even then that is usually
 * an implementation and uses DI).
 *
 * This process is facilitated by various instance providers - most of which is described at the wiki entry on
 * [Dependency Injection](https://nextfaze.github.io/dev-fun/wiki/-dependency%20-injection.html).
 *
 * To quickly and simply provide a single object type, use [captureInstance], which creates a
 * [CapturingInstanceProvider] that can be added to the root (composite) instance provider at `DevFun.instanceProviders`.
 * e.g.
 * ```kotlin
 * class SomeType : BaseType
 *
 * val provider = captureInstance { someObject.someType } // triggers for SomeType or BaseType
 * ```
 *
 * If you want to reduce the type range then specify its base type manually:
 * ```kotlin
 * val provider = captureInstance<BaseType> { someObject.someType } // triggers only for BaseType
 * ```
 *
 * _Be aware of leaks! The lambda could implicitly hold a local `this` reference._
 *
 * @see RequiringInstanceProvider
 */
interface InstanceProvider {
    /**
     * Try to get an instance of some [clazz].
     *
     * @return An instance of [clazz], or `null` if this provider can not handle the type
     *
     * @see RequiringInstanceProvider.get
     */
    operator fun <T : Any> get(clazz: KClass<out T>): T?
}

/**
 * Same as [InstanceProvider], but throws [ClassInstanceNotFoundException] instead of returning `null`.
 *
 * _(TODO: Think of better nomenclature?)_
 */
interface RequiringInstanceProvider : InstanceProvider {
    /**
     * Get an instance of some [clazz].
     *
     * @return An instance of [clazz]
     *
     * @throws ClassInstanceNotFoundException When [clazz] could not be found/instantiated
     */
    override operator fun <T : Any> get(clazz: KClass<out T>): T
}

/**
 * Exception thrown when attempting to provide a type that was not found from any [InstanceProvider].
 */
class ClassInstanceNotFoundException : Exception {
    constructor(clazz: KClass<*>) : super("Failed to get instance of $clazz")
    constructor(msg: String) : super(msg)
}

/**
 * An instance provider that requests an instance of a class from a captured lambda.
 *
 * Be aware of leaks! The lambda could implicitly hold a local `this` reference.
 *
 * @see captureInstance
 */
class CapturingInstanceProvider<out T : Any>(private val instanceClass: KClass<T>, private val instance: () -> T?) : InstanceProvider {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(clazz: KClass<out T>) = when {
        clazz.isSuperclassOf(instanceClass) -> instance() as T?
        else -> null
    }
}

/**
 * Utility function to capture an instance of an object.
 *
 * e.g.
 * ```kotlin
 * class SomeType : BaseType
 *
 * val provider = captureInstance { someObject.someType } // triggers for SomeType or BaseType
 * ```
 *
 * If you want to reduce the type range then specify its base type manually:
 * ```kotlin
 * val provider = captureInstance<BaseType> { someObject.someType } // triggers only for BaseType
 * ```
 *
 * @see CapturingInstanceProvider
 */
inline fun <reified T : Any> captureInstance(noinline instance: () -> T?): InstanceProvider = CapturingInstanceProvider(T::class, instance)

/**
 * Tag to allow classes to be instantiated when no other [InstanceProvider] was able to provide the class.
 *
 * The class must have only one constructor. Any arguments to the constructor will be injected as normal.
 *
 * In general this should not be used (you should be using your own dependency injection framework).
 * However for quick-n-dirty uses this can make life a bit easier (e.g. function transformers which are debug only anyway).
 *
 * @see FunctionTransformer
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Constructable

private fun <T : Any> KClass<*>.isSuperclassOf(clazz: KClass<T>) = this.java.isAssignableFrom(clazz.java)
