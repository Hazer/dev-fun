[gh-pages](../../index.md) / [com.nextfaze.devfun.menu.controllers](../index.md) / [CogOverlay](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`CogOverlay(context: `[`Context`](https://developer.android.com/reference/android/content/Context.html)`, activityProvider: `[`ActivityProvider`](../../com.nextfaze.devfun.core/-activity-provider.md)`, overlays: `[`OverlayManager`](../../com.nextfaze.devfun.overlay/-overlay-manager/index.md)`)`

Controls the floating cog overlay.

Manages/requests permissions as needed, and hides/shows when app view context changes.

Background color/tint of the cog can be changed by declaring (overriding) a color resource `df_menu_cog_tint`

e.g.

``` xml
<color name="df_menu_cog_tint">#77FF0000</color> <!-- red -->
```

