package com.nextfaze.devfun.menu.controllers

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AlertDialog
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.ImageView
import com.nextfaze.devfun.annotations.DeveloperCategory
import com.nextfaze.devfun.annotations.DeveloperFunction
import com.nextfaze.devfun.core.ActivityProvider
import com.nextfaze.devfun.core.devFun
import com.nextfaze.devfun.inject.Constructable
import com.nextfaze.devfun.internal.android.*
import com.nextfaze.devfun.internal.log.*
import com.nextfaze.devfun.internal.pref.*
import com.nextfaze.devfun.internal.string.*
import com.nextfaze.devfun.invoke.view.ColorPicker
import com.nextfaze.devfun.invoke.view.From
import com.nextfaze.devfun.invoke.view.Ranged
import com.nextfaze.devfun.invoke.view.ValueSource
import com.nextfaze.devfun.menu.DeveloperMenu
import com.nextfaze.devfun.menu.MenuController
import com.nextfaze.devfun.menu.R
import com.nextfaze.devfun.menu.devMenu
import com.nextfaze.devfun.overlay.Dock
import com.nextfaze.devfun.overlay.OverlayManager

/**
 * Controls the floating cog overlay.
 *
 * Manages/requests permissions as needed, and hides/shows when app view context changes.
 *
 * Background color/tint of the cog can be changed by declaring (overriding) a color resource `df_menu_cog_tint`
 *
 * e.g.
 * ```xml
 * <color name="df_menu_cog_tint">#77FF0000</color> <!-- red -->
 * ```
 */
@DeveloperCategory("DevFun", "Cog Overlay")
class CogOverlay constructor(
    context: Context,
    private val activityProvider: ActivityProvider,
    private val overlayManager: OverlayManager
) : MenuController {
    private val log = logger()
    private val application = context.applicationContext as Application

    private val activity get() = activityProvider()
    private val fragmentActivity get() = activity as? FragmentActivity

    private val overlay =
        overlayManager.createOverlay(
            layoutId = R.layout.df_menu_cog_overlay,
            prefsName = "DevFunCog",
            reason = ::generateCogDescriptionState,
            onClick = ::onOverlayClick,
            visibilityPredicate = { it is FragmentActivity },
            initialDock = Dock.RIGHT,
            initialDelta = 0.7f
        ).apply {
            val padding = application.resources.getDimension(R.dimen.df_menu_cog_padding).toInt()
            val halfSize = ((padding * 2 + application.resources.getDimension(R.dimen.df_menu_cog_size)) / 2).toInt()
            viewInset = Rect(halfSize, halfSize, halfSize, padding)
        }

    private var listener: Application.ActivityLifecycleCallbacks? = null
    private var developerMenu: DeveloperMenu? = null

    private val preferences = KSharedPreferences.named(context, "DevFunCog")
    private val cogColorPref = preferences["cogColor", defaultTint]
    private var cogColor by cogColorPref

    override fun attach(developerMenu: DeveloperMenu) {
        this.developerMenu = developerMenu

        listener = application.registerActivityCallbacks(
            onResumed = { updateAppearance(it) }
        )
    }

    override fun detach() {
        overlayManager.destroyOverlay(overlay)
        listener?.unregister(application).also { listener = null }
        developerMenu = null
    }

    override val title: String get() = application.getString(R.string.df_menu_cog_overlay)
    override val actionDescription: CharSequence?
        get() = mutableListOf<Int>()
            .apply {
                if (!overlay.enabled) {
                    this += R.string.df_menu_cog_overlay_hidden_by_user
                }
                if (!overlay.canDrawOverlays) {
                    this += R.string.df_menu_cog_overlay_no_permissions
                }
                if (isEmpty()) {
                    this += R.string.df_menu_cog_tap_to_show
                }
            }
            .joinTo(SpannableStringBuilder(), "\n") { resId ->
                SpannableStringBuilder().also {
                    it += " • "
                    it += application.getText(resId)
                }
            }

    override fun onShown() = Unit
    override fun onDismissed() = Unit

    private fun updateAppearance(activity: Activity) {
        with(overlay.view) {
            findViewById<View>(R.id.cogButton).apply {
                ViewCompat.setElevation(this, resources.getDimensionPixelSize(R.dimen.df_menu_cog_elevation).toFloat())
                tintOverlayView(resolveTint(activity))
            }
        }
    }

    /**
     * This function was extracted like this as the activity parameter is leaked from with the original code:
     * ```kotlin
     * val developerMenu = developerMenu ?: return
     * onClick = { fragmentActivity?.let(developerMenu::show) }
     * ```
     * (bug has been reported - the onClick lambda is incorrectly generated)
     * https://youtrack.jetbrains.com/issue/KT-23881
     */
    private fun onOverlayClick(@Suppress("UNUSED_PARAMETER") view: View) {
        developerMenu?.also { menu ->
            fragmentActivity?.also { activity ->
                menu.show(activity)
            }
        }
    }

    @DeveloperFunction
    private fun resetPositionAndState(activity: Activity) {
        preferences.clear()
        overlay.apply {
            resetPositionAndState()
            removeFromWindow()
            updateAppearance(activity)
            addToWindow()
        }
    }

    @DeveloperFunction
    private fun resetColor(activity: Activity) {
        cogColorPref.delete()
        tintOverlayView(resolveTint(activity))
    }

    @Constructable
    private inner class CurrentVisibility : ValueSource<Boolean> {
        override val value get() = overlay.enabled
    }

    private fun generateCogDescriptionState() =
        SpannableStringBuilder().apply {
            this += application.getText(R.string.df_menu_available_controllers)
            this += "\n\n"
            this += devFun.devMenu.actionDescription ?: application.getString(R.string.df_menu_no_controllers)
        }

    @DeveloperFunction
    private fun setVisibility(@From(CurrentVisibility::class) visible: Boolean) {
        overlay.enabled = visible
        activity?.let {
            if (!visible) {
                AlertDialog.Builder(it)
                    .setTitle(R.string.df_menu_cog_overlay_hidden)
                    .setMessage(generateCogDescriptionState())
                    .show()
            }
        }
    }

    @Constructable
    private inner class CurrentColor(private val activity: Activity) : ValueSource<Int> {
        override val value get() = resolveTint(activity)
    }

    @DeveloperFunction
    private fun setColor(@ColorPicker @From(CurrentColor::class) color: Int) {
        cogColor = color
        tintOverlayView(cogColor)
    }

    @Constructable
    private inner class CurrentAlpha(private val activity: Activity) : ValueSource<Int> {
        override val value get() = Color.alpha(resolveTint(activity))
    }

    @DeveloperFunction
    private fun setAlpha(activity: Activity, @Ranged(to = 255.0) @From(CurrentAlpha::class) alpha: Int) {
        val c = resolveTint(activity)
        cogColor = Color.argb(alpha, Color.red(c), Color.green(c), Color.blue(c))
        tintOverlayView(cogColor)
    }

    private fun tintOverlayView(color: Int) {
        val alpha = Color.alpha(color)
        val noAlphaColor = Color.rgb(Color.red(color), Color.green(color), Color.blue(color))

        if (Build.VERSION.SDK_INT >= 21) {
            // we need to separate it out otherwise the img gets a weird alpha shadow effect
            overlay.view.alpha = alpha / 255f
            overlay.view.findViewById<View>(R.id.cogButton).apply {
                DrawableCompat.setTint(DrawableCompat.wrap(background), noAlphaColor)
            }
        } else {
            // older SDKs don't support setting the alpha value on a ViewGroup
            // so we do it to the ImageView instead (doesn't look as nice though)
            overlay.view.findViewById<ImageView>(R.id.cogButton).apply {
                DrawableCompat.setTint(DrawableCompat.wrap(background), color)
                when {
                    Build.VERSION.SDK_INT >= 16 -> imageAlpha = alpha
                    else -> @Suppress("DEPRECATION") setAlpha(alpha)
                }
            }
        }

        // required for older devices
        overlay.view.invalidate()
    }

    @DeveloperFunction
    private fun showOverlayState(activity: Activity) =
        overlay.toString()
            .also {
                AlertDialog.Builder(activity)
                    .setTitle("Overlay State")
                    .setMessage(
                        SpannableStringBuilder().apply {
                            this += pre(it)
                        }
                    )
                    .show()
            }

    private fun resolveTint(activity: Activity) =
        try {
            if (cogColorPref.isSet) {
                // user has overridden value manually
                cogColor.also {
                    log.d { "Using custom tint set by user: ${it.toColorStruct()}" }
                }
            } else {
                // if the use has defined their own tint
                ContextCompat.getColor(activity, R.color.df_menu_cog_tint).also {
                    log.d { "Using user defined resource 'df_menu_cog_tint' for cog overlay tint: ${it.toColorStruct()}" }
                }
            }
        } catch (ignore: Resources.NotFoundException) {
            log.t { "Override resource tint 'df_menu_cog_tint' not defined. Trying theme accent color..." }

            try {
                // otherwise use app accent color if defined
                val ta = activity.theme.obtainStyledAttributes(intArrayOf(android.support.v7.appcompat.R.attr.colorAccent))
                try {
                    if (ta.hasValue(0)) {
                        ta.getColor(0, defaultTint).let {
                            when {
                                Color.alpha(it) == 0xFF -> (it.toLong() and 0x77FFFFFF).toInt()
                                else -> it
                            }
                        }.also { log.t { "Using primary color attribute for tint: ${it.toColorStruct()}" } }
                    } else {
                        defaultTint.also {
                            log.d { "No appcompat 'colorPrimary' attribute defined. Using default tint value: ${it.toColorStruct()}" }
                        }
                    }
                } finally {
                    ta.recycle()
                }
            } catch (t: Throwable) {
                log.w(t) { "Unexpected error when trying to resolve cog overlay tint - falling back to default tint value: ${defaultTint.toColorStruct()}" }
                // else fall back to default tint
                defaultTint
            }
        }

    private fun Int.toColorStruct(): String {
        fun Int.toHexString() = Integer.toHexString(this)

        val a = Color.alpha(this)
        val (r, g, b) = Triple(Color.red(this), Color.green(this), Color.blue(this))
        return """$this as color:
            |color {
            |  components {
            |    alpha: $a ($${a.toHexString()})
            |    red: $r ($${r.toHexString()})
            |    green: $g ($${g.toHexString()})
            |    blue: $b ($${b.toHexString()})
            |  }
            |  code: #${this.toHexString()}
            |}""".trimMargin()
    }
}

private val defaultTint = Color.argb(0x77, 0xEE, 0x41, 0x36)
