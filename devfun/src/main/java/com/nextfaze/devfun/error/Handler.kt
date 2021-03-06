package com.nextfaze.devfun.error

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.nextfaze.devfun.category.CategoryDefinition
import com.nextfaze.devfun.category.DeveloperCategory
import com.nextfaze.devfun.core.ActivityProvider
import com.nextfaze.devfun.function.DeveloperFunction
import com.nextfaze.devfun.function.FunctionDefinition
import com.nextfaze.devfun.function.FunctionItem
import com.nextfaze.devfun.function.FunctionTransformer
import com.nextfaze.devfun.function.SimpleFunctionItem
import com.nextfaze.devfun.function.SingleFunctionTransformer
import com.nextfaze.devfun.inject.Constructable
import com.nextfaze.devfun.internal.android.*
import com.nextfaze.devfun.internal.exception.stackTraceAsString
import com.nextfaze.devfun.internal.log.*
import com.nextfaze.devfun.internal.string.*
import kotlinx.android.parcel.Parcelize
import java.util.Date

/**
 * Details/information of an error.
 *
 * @see SimpleError
 */
interface ErrorDetails {
    /** When this error occurred in millis since epoch. */
    val time: Long

    /** The exception that was thrown. */
    val t: Throwable

    /** A title for the dialog - the "kind" if you will. */
    val title: CharSequence

    /** Some details about why it occurred and/or resolution details, etc. */
    val body: CharSequence

    /** The function item to lead to this error (such as when attempting to invoke/prepare/whatever). Will be `null` for general errors. */
    val functionItem: FunctionItem?
}

/**
 * Convenience class that implements [ErrorDetails] and automatically time stamps it.
 *
 * @see ErrorDetails.time
 */
data class SimpleError(
    override val t: Throwable,
    override val title: CharSequence,
    override val body: CharSequence,
    override val functionItem: FunctionItem? = null,
    override val time: Long = System.currentTimeMillis()
) : ErrorDetails

/**
 * Handles errors that occur during/throughout DevFun.
 *
 * You should use this in your own modules to provide consistent error handling.
 * It's unlikely you'll need to implement this yourself.
 *
 * The default error handler will show a dialog with the exception stack trace and some error details.
 */
interface ErrorHandler {
    /**
     * Log a simple warning message.
     *
     * @param title A title for the message/dialog.
     * @param body A short description of how/why this exception was thrown.
     */
    fun onWarn(title: CharSequence, body: CharSequence)

    /**
     * Call to log an error.
     *
     * This could be anything from reading/processing throughout DevFun, to more specific scenarios such as when
     * invoking a [FunctionItem].
     *
     * This function simply delegates to [ErrorHandler.onError].
     *
     * @param t The exception that occurred.
     * @param title A title for the message/dialog.
     * @param body A short description of how/why this exception was thrown.
     * @param functionItem The relevant function item that lead to this error occurring (or `null`/absent) if not relevant.
     */
    fun onError(t: Throwable, title: CharSequence, body: CharSequence, functionItem: FunctionItem? = null)

    /**
     * Call to log an error.
     *
     * This could be anything from reading/processing throughout DevFun, to more specific scenarios such as when
     * invoking a [FunctionItem].
     *
     * @param error The error details.
     */
    fun onError(error: ErrorDetails)

    /**
     * Mark an error as seen by the user.
     *
     * @param key To error key - as defined by the [ErrorHandler] implementation.
     */
    fun markSeen(key: Any)

    /**
     * Remove an error from the history.
     *
     * @param key To error key - as defined by the [ErrorHandler] implementation.
     */
    fun remove(key: Any)

    /** Clears all errors, seen or otherwise. */
    fun clearAll()
}

/**
 * Used during the initialization phase for if/when we can't get an instance of [ErrorHandler].
 *
 * Logs using [Log]
 */
internal object BasicErrorLogger {
    fun onError(context: Context?, ref: Any, message: String, t: Throwable) {
        Log.e(ref::class.java.name, message, t)
        if (context != null) {
            Toast.makeText(context.applicationContext, "$message (see logs):\n${t.message}", Toast.LENGTH_LONG).show()
        }
    }
}

@DeveloperCategory("DevFun")
@Constructable(singleton = true)
internal class DefaultErrorHandler(application: Application, private val activityProvider: ActivityProvider) : ErrorHandler {
    private val log = logger()
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private val activity get() = activityProvider() as? FragmentActivity

    private val errorLock = Any()
    private var errors = mutableMapOf<Any, RenderedError>()

    private val showLock = Any()
    private var showCallback: (() -> Unit)? = null

    init {
        application.registerActivityCallbacks(
            onResumed = { showErrorDialogIfHaveUnseen() }
        )
    }

    override fun onWarn(title: CharSequence, body: CharSequence) =
        onError(SimpleError(RuntimeException("Warning: $title"), title, body))

    override fun onError(t: Throwable, title: CharSequence, body: CharSequence, functionItem: FunctionItem?) =
        onError(SimpleError(t, title, body, functionItem))

    override fun onError(error: ErrorDetails) {
        log.e(error.t) {
            """DevFun Error
                |title: ${error.title}
                |body: ${error.body}
                |when: ${Date(error.time)}
                |functionItem: ${if (error.functionItem == null) "NA" else "${error.functionItem}"}
                |exception:
                |""".trimMargin()
        }

        val renderedError = error.render()
        synchronized(errorLock) {
            errors[renderedError.nanoTime] = renderedError
        }

        postShowErrorDialog()
    }

    override fun markSeen(key: Any) {
        synchronized(errorLock) {
            errors[key]?.seen = true
        }
    }

    override fun remove(key: Any) {
        synchronized(errorLock) {
            errors.remove(key)
        }
    }

    override fun clearAll() {
        synchronized(errorLock) {
            errors.clear()
        }
    }

    @Constructable(singleton = true)
    private inner class ShowErrorDialogVisibility : VisibilityTransformer() {
        override val predicate = { synchronized(errorLock) { errors.isNotEmpty() } }
    }

    @DeveloperFunction(transformer = ShowErrorDialogVisibility::class)
    private fun showErrorDialog() = showErrorDialogIfHaveUnseen(true)

    private fun postShowErrorDialog() {
        synchronized(showLock) {
            if (showCallback != null) return

            showCallback = { showErrorDialogIfHaveUnseen() }
            handler.post {
                synchronized(showLock) {
                    showCallback?.let {
                        showCallback = null
                        it()
                    }
                }
            }
        }
    }

    private fun showErrorDialogIfHaveUnseen(force: Boolean = false) {
        val dialogErrors = synchronized(errorLock) {
            errors.takeIf { it.isNotEmpty() }?.takeIf { force || it.values.any { e -> !e.seen } }?.values?.mapTo(ArrayList()) { it }
        } ?: return
        activity?.also {
            ErrorDialogFragment.show(it, dialogErrors)
        }
    }

    @Constructable(singleton = true)
    private inner class ShowErrorsTransformer : FunctionTransformer {
        override fun apply(functionDefinition: FunctionDefinition, categoryDefinition: CategoryDefinition): List<SimpleFunctionItem>? {
            val errorCount = synchronized(errorLock) { errors.size }.takeIf { it > 0 } ?: return emptyList()
            val category = object : CategoryDefinition {
                override val name = SpannableStringBuilder().apply { this += b("Errors ($errorCount)") }
                override val order = -9_000 // under Context
            }

            fun createFunctionItem(name: String, group: String? = null, action: ErrorHandler.() -> Unit) =
                object : SimpleFunctionItem(functionDefinition, categoryDefinition) {
                    override val name = name
                    override val group = group
                    override val category = category
                    override val args = listOf(action)
                }

            return listOf(
                createFunctionItem("Show Error Dialog", "Errors") { showErrorDialog() },
                createFunctionItem("Clear All") { clearAll() }
            )
        }
    }

    @DeveloperFunction(transformer = ShowErrorsTransformer::class)
    private fun manageErrors(action: ErrorHandler.() -> Unit) = action()
}

/**
 * TODO consider making this a publicly accessible utility class?
 *
 * Perhaps add annotation for the function argument @Visibility or something (to avoid need of custom transform implementation)?
 */
private abstract class VisibilityTransformer(
    private val transformer: FunctionTransformer = SingleFunctionTransformer
) : FunctionTransformer {
    override fun apply(functionDefinition: FunctionDefinition, categoryDefinition: CategoryDefinition) =
        if (predicate()) transformer.apply(functionDefinition, categoryDefinition) else emptyList()

    abstract val predicate: () -> Boolean
}

private fun ErrorDetails.render(): RenderedError =
    RenderedErrorImpl(
        System.nanoTime(),
        System.currentTimeMillis(),
        t.stackTraceAsString,
        title,
        body,
        functionItem?.function?.method?.toString(),
        false
    )

@Parcelize
private data class RenderedErrorImpl(
    override val nanoTime: Long,
    override val timeMs: Long,
    override val stackTrace: String,
    override val title: CharSequence,
    override val body: CharSequence,
    override val method: CharSequence?,
    override var seen: Boolean
) : RenderedError
