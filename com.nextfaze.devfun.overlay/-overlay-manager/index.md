[gh-pages](../../index.md) / [com.nextfaze.devfun.overlay](../index.md) / [OverlayManager](./index.md)

# OverlayManager

`interface OverlayManager` [(source)](https://github.com/NextFaze/dev-fun/tree/master/devfun/src/main/java/com/nextfaze/devfun/overlay/Overlays.kt#L26)

Handles creation, destruction, and visibility of overlays.

**See Also**

[OverlayWindow](../-overlay-window/index.md)

[OverlayPermissions](../-overlay-permissions/index.md)

### Properties

| Name | Summary |
|---|---|
| [canDrawOverlays](can-draw-overlays.md) | `abstract val canDrawOverlays: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Flag indicating if the user has granted Overlay Permissions. |

### Functions

| Name | Summary |
|---|---|
| [configureOverlay](configure-overlay.md) | `abstract fun configureOverlay(overlayWindow: `[`OverlayWindow`](../-overlay-window/index.md)`, additionalOptions: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`UiField`](../../com.nextfaze.devfun.invoke/-ui-field/index.md)`<*>> = emptyList(), onResetClick: `[`OnClick`](../../com.nextfaze.devfun.invoke/-on-click.md)` = { overlayWindow.resetPositionAndState() }): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Show a configuration dialog for an overlay. |
| [createOverlay](create-overlay.md) | `abstract fun createOverlay(layoutId: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, prefsName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, reason: `[`OverlayReason`](../-overlay-reason.md)`, onClick: `[`ClickListener`](../-click-listener.md)`? = null, onLongClick: `[`ClickListener`](../-click-listener.md)`? = null, onVisibilityChange: `[`VisibilityListener`](../-visibility-listener.md)`? = null, onAttachChange: `[`AttachListener`](../-attach-listener.md)`? = null, visibilityPredicate: `[`VisibilityPredicate`](../-visibility-predicate.md)`? = null, visibilityScope: `[`VisibilityScope`](../-visibility-scope/index.md)` = VisibilityScope.FOREGROUND_ONLY, dock: `[`Dock`](../-dock/index.md)` = Dock.TOP_LEFT, delta: `[`Float`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)` = 0f, snapToEdge: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, left: `[`Float`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)` = 0f, top: `[`Float`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)` = 0f, enabled: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true): `[`OverlayWindow`](../-overlay-window/index.md)<br>Creates an overlay window. |
| [destroyOverlay](destroy-overlay.md) | `abstract fun destroyOverlay(overlayWindow: `[`OverlayWindow`](../-overlay-window/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Destroy an overlay instance (cleans up any listeners/callbacks/resources/etc). |
