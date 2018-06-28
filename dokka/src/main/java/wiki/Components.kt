package wiki

import com.nextfaze.devfun.annotations.Dagger2Component
import com.nextfaze.devfun.annotations.Dagger2Scope
import com.nextfaze.devfun.annotations.DeveloperCategory
import com.nextfaze.devfun.annotations.DeveloperFunction
import com.nextfaze.devfun.compiler.DevFunProcessor
import com.nextfaze.devfun.core.DevFun
import com.nextfaze.devfun.core.FunctionItem
import com.nextfaze.devfun.core.call
import com.nextfaze.devfun.core.devFun
import com.nextfaze.devfun.httpd.DevHttpD
import com.nextfaze.devfun.httpd.devDefaultPort
import com.nextfaze.devfun.httpd.frontend.HttpFrontEnd
import com.nextfaze.devfun.inject.CompositeInstanceProvider
import com.nextfaze.devfun.inject.InstanceProvider
import com.nextfaze.devfun.inject.dagger2.InjectFromDagger2
import com.nextfaze.devfun.inject.dagger2.tryGetInstanceFromComponent
import com.nextfaze.devfun.inject.dagger2.useAutomaticDagger2Injector
import com.nextfaze.devfun.invoke.view.ColorPicker
import com.nextfaze.devfun.menu.DevMenu
import com.nextfaze.devfun.menu.MenuController
import com.nextfaze.devfun.menu.controllers.CogOverlay
import com.nextfaze.devfun.menu.controllers.KeySequence
import com.nextfaze.devfun.stetho.DevStetho
import com.nextfaze.devfun.utils.glide.GlideUtils
import com.nextfaze.devfun.utils.leakcanary.LeakCanaryUtils
import java.util.ServiceLoader

/**
DevFun is designed to be modular, in terms of both its dependencies (limiting impact to main source tree) and its plugin-like architecture. ![Component Dependencies](https://github.com/NextFaze/dev-fun/raw/gh-pages/assets/uml/components.png)

 * <!-- START doctoc generated TOC please keep comment here to allow auto update -->
 * <!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
 * 
 * 
 * - [Main Modules](#main-modules)
 *     - [Annotations](#annotations)
 *     - [Compiler](#compiler)
 *     - [Gradle Plugin](#gradle-plugin)
 * - [Core Modules](#core-modules)
 *     - [DevFun](#devfun)
 *     - [Menu](#menu)
 * - [Inject Modules](#inject-modules)
 *     - [Dagger 2](#dagger-2)
 *         - [Reflection Based](#reflection-based)
 *         - [Annotation Based](#annotation-based)
 *         - [Custom Instance Provider](#custom-instance-provider)
 * - [Util Modules](#util-modules)
 *     - [Glide](#glide)
 *     - [Leak Canary](#leak-canary)
 * - [Invoke Modules](#invoke-modules)
 *     - [Color Picker View](#color-picker-view)
 * - [Experimental Modules](#experimental-modules)
 *     - [HttpD](#httpd)
 *         - [Custom Port](#custom-port)
 *     - [Http Front-end](#http-front-end)
 *     - [Stetho](#stetho)
 * 
 * <!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Main Modules
Minimum required libraries - annotations and annotation processor.


`IMG_START<img src="https://github.com/NextFaze/dev-fun/raw/gh-pages/assets/gif/enable-sign-in.gif" alt="DevFun demonstration" width="35%" align="right"/>IMG_END`
### Annotations
Contains the annotations [DeveloperFunction] and [DeveloperCategory], and various interface definitions.

This library contains primarily interface definitions and inline functions, and will have a
negligible impact on your method count and dex sizes. Apply to your main `compile` configuration:
```gradle
implementation 'com.nextfaze.devfun:devfun-annotations:1.2.0'
```


### Compiler
Annotation processor [DevFunProcessor] that handles [DeveloperFunction] and [DeveloperCategory] annotations.

This should be applied to your non-main kapt configuration 'kaptDebug' to avoid running/using it on release builds.
```gradle
kaptDebug 'com.nextfaze.devfun:devfun-compiler:1.2.0'
```

Configuration options can be applied using Android DSL:
 * ```gradle
 *  android {
 *       defaultConfig {
 *           javaCompileOptions {
 *               annotationProcessorOptions {
 *                   argument 'devfun.argument', 'value'
 *               }
 *           }
 *       }
 *  }
 * ```
Full list available at [com.nextfaze.devfun.compiler].



### Gradle Plugin
Used to configure/provide the compiler with the project/build configurations.

In your `build.gradle` add the DevFun Gradle plugin to your build script.

If you can use the Gradle `plugins` block (which you should be able to do - this locates and downloads it for you):
 * ```groovy
 * plugins {
 *     id 'com.nextfaze.devfun' version '1.2.0'
 * }
 * ```

__Or__ the legacy method using `apply`;
Add the plugin to your classpath (found in the `jcenter()` repository):
 * ```groovy
 * buildscript {
 *     dependencies {
 *         classpath 'com.nextfaze.devfun:devfun-gradle-plugin:1.2.0'
 *     }
 * }
 * ```

And in your `build.gradle`:
```groovy
apply plugin: 'com.nextfaze.devfun'
```



## Core Modules
Modules that extend the accessibility of DevFun (e.g. add menu/http server).

_Also see [Experimental Modules](#experimental-modules) below._


### DevFun
Core of [DevFun]. Loads modules and definitions.

Apply to your non-main configuration:
```gradle
debugImplementation 'com.nextfaze.devfun:devfun:1.2.0'
```

Modules are loaded by [DevFun] using Java's [ServiceLoader].

[DevFun] loads, transforms, and sorts the generated function definitions, again via the [ServiceLoader] mechanism.
To inject function invocations, [InstanceProvider]s are used, which will attempt to locate (or create) object instances.
A composite instance provider [CompositeInstanceProvider] at [DevFun.instanceProviders] is used via convenience
function (extension) [FunctionItem.call] that uses the currently loaded [devFun] instance.

If using Dagger 2.x, you can use the `devfun-inject-dagger2` module for a simple reflection based provider or related helper
functions. A heavily reflective version will be used automatically, but if it fails (e.g. it expects a `Component` in
your application class), a manual implementation can be provided.
See the demo app [DemoInstanceProvider](https://github.com/NextFaze/dev-fun/tree/master/demo/src/debug/java/com/nextfaze/devfun/demo/devfun/DevFun.kt#L52) for a sample implementation.


`IMG_START<img src="https://github.com/NextFaze/dev-fun/raw/gh-pages/assets/gif/registration-flow.gif" alt="Menu demonstration" width="35%" align="right"/>IMG_END`
### Menu
Adds a developer menu [DevMenu], accessible by a floating cog [CogOverlay] (long-press to drag) or device button sequence [KeySequence].
```gradle
debugImplementation 'com.nextfaze.devfun:menu:1.2.0'
```

Button sequences: *(this are not configurable at the moment but are intended to be eventually)*
 * ```kotlin
 * internal val GRAVE_KEY_SEQUENCE = KeySequence.Definition(
 *     keyCodes = intArrayOf(KeyEvent.KEYCODE_GRAVE),
 *     description = R.string.df_menu_grave_sequence,
 *     consumeEvent = true
 * )
 * internal val VOLUME_KEY_SEQUENCE = KeySequence.Definition(
 *     keyCodes = intArrayOf(
 *         KeyEvent.KEYCODE_VOLUME_DOWN,
 *         KeyEvent.KEYCODE_VOLUME_DOWN,
 *         KeyEvent.KEYCODE_VOLUME_UP,
 *         KeyEvent.KEYCODE_VOLUME_DOWN
 *     ),
 *     description = R.string.df_menu_volume_sequence,
 *     consumeEvent = false
 * )
 * ```

Menu controllers implement [MenuController] and can be added via `devFun.module<DevMenu>() += MyMenuController()`.



## Inject Modules
Modules to facilitate dependency injection for function invocation.


### Dagger 2
Adds module [InjectFromDagger2] which adds an [InstanceProvider] that can reflectively locate Dagger 2 components or (if used) resolve
[Dagger2Component] uses.
```gradle
debugImplementation 'com.nextfaze.devfun:devfun-inject-dagger2:1.2.0'
```

It only really supports simple graphs by finding provides methods/fields that match (or are a super type) of the requested type (scoping is
not well handled).

The module also provides a variety of utility functions for manually providing your own instance provider using your components. See below
for more details.

_I'm always looking into better ways to support this, comments/suggestions are welcome._
- Currently kapt doesn't support multi-staged processing of generated Kotlin code.
- Possibly consider generating Java `Component` interfaces for some types?

#### Reflection Based
By default simply including the module will use the reflection-based component locator.

It will attempt to locate your component objects in your application class and/or your activity classes and use aforementioned utility
functions.

If you place one or more [Dagger2Component] annotations (see below), then the reflective locator wont be used.

#### Annotation Based
For more control, or if the above method doesn't (such as if you use top-level extension functions to retrieve your components, or you put
them in weird places, or for whatever reason), then you can annotate the functions/getters with [Dagger2Component].
The scope/broadness/priority can be set on the annotation either via [Dagger2Component.scope] or [Dagger2Component.priority].
If unset then the scope will be assumed based on the context of its location (i.e. in Application class > probably the top level component,
if static then first argument assumed to be the receiver, etc).

Note: For properties you can annotated to property itself (`@Dagger2Component`) or the getter explicitly (`@get:Dagger2Component`) if for
some reason on the property doesn't work (which could happen if it can't find your getter - which is done via method name string
manipulation due to KAPT limitations.

Example usage:
- Where a top-level/singleton/application component is retrieved via an extension function _(from the demo)_:
 * ```kotlin
 * @Dagger2Component
 * val Context.applicationComponent: ApplicationComponent?
 *     get() = (applicationContext as DaggerApplication).applicationComponent
 *```

- Where a component is kept in the activity _(from the demo)_:
 * ```kotlin
 * @get:Dagger2Component // if we want to specify getter explicitly
 * lateinit var activityComponent: ActivityComponent
 *     private set
 * ```

- Where a differently scoped component is also kept in the activity, we can set the scope manually ([Dagger2Scope]) _(from the demo)_:
 * ```kotlin
 * @get:Dagger2Component(Dagger2Scope.RETAINED_FRAGMENT)
 * lateinit var retainedComponent: RetainedComponent
 *     private set
 * ```

#### Custom Instance Provider
Since the reflection locator and annotation based still make assumptions and are bit inefficient because of it, sometimes you may need to
implement your own instance provider.

- Disable the automatic locator: set [useAutomaticDagger2Injector] to `false` (can be done at any time).
- Add your own provider using `devFun += MyProvider` (see [InstanceProvider] for more details).
- Utility function [tryGetInstanceFromComponent] to help (though again it relies heavily on reflection and don't consider scoping very well).

See demo for example implementation: [DemoInstanceProvider](https://github.com/NextFaze/dev-fun/tree/master/demo/src/debug/java/com/nextfaze/devfun/demo/devfun/DevFun.kt#L52)



## Util Modules
Modules with frequently used or just handy functions (e.g. show Glide memory use).

Developers love reusable utility functions, we all have them, and we usually copy-paste them into new projects.
Adding them to modules and leveraging dependency injection allows for non-static, easily invokable code reuse.

_Still playing with this concept and naming conventions etc._


### Glide
Module [GlideUtils] provides some utility functions when using Glide.
```gradle
debugImplementation 'com.nextfaze.devfun:devfun-util-glide:1.2.0'
```

Features:
- Clear memory cache
- Clear disk cache
- Log current memory/disk cache usage


### Leak Canary
Module [LeakCanaryUtils] provides some utility functions when using Leak Canary.
```gradle
debugImplementation 'com.nextfaze.devfun:devfun-util-leakcanary:1.2.0'
```

Features:
- Launch `DisplayLeakActivity`



`IMG_START<img src="https://github.com/NextFaze/dev-fun/raw/gh-pages/assets/images/color-picker.png" alt="Invocation UI with custom color picker view" width="35%" align="right"/>IMG_END`
## Invoke Modules
Modules to facilitate function invocation.

### Color Picker View
Adds a parameter annotation [ColorPicker] that lets the invocation UI render a color picker view for the associated argument.

_Note: Only needed if you don't include `devfun-menu` (as it uses/includes the color picker transitively)._
```gradle
debugImplementation 'com.nextfaze.devfun-invoke-view-colorpicker:1.2.0'
```



## Experimental Modules
These modules are mostly for use experimenting with various use-cases.

They generally work, but are likely to be buggy and have various limitations and nuances.
Having said that, it would be nice to expand upon them and make them nicer/more feature reach in the future.

Some future possibilities:
- HttpD: Use ktor instead of Nano
- HttpD Simple Index: Provide a themed react page or something pretty/nice
- Add a Kotlin REPL module


### HttpD
Module [DevHttpD] adds a local HTTP server (uses [NanoHttpD](https://github.com/NanoHttpd/nanohttpd)).

Provides a single `POST` method `invoke` with one parameter `hashCode` (expecting [FunctionItem.hashCode])
```gradle
debugImplementation 'com.nextfaze.devfun:httpd:1.2.0'
```

Use with HttpD Front-end.
_Current port and access instructions are logged on start._

Default port is `23075`.
If this is in use another is deterministically generated from your package (this might become the default).

If using AVD, forward port:
```bash
adb forward tcp:23075 tcp:23075
```

Then access via IP: [http://127.0.0.1:23075/](http://127.0.0.1:23075/)

#### Custom Port
Can be set via resources:
```xml
<integer name="df_httpd_default_port">12345</integer>
```

Or before initialization [devDefaultPort] (i.e. if not using auto-init content provider):
```kotlin
devDefaultPort = 12345 // top level value located in com.nextfaze.devfun.httpd
```


### Http Front-end
Module [HttpFrontEnd] generates an admin interface using SB Admin 2 (similar to [DevMenu]), allowing function invocation from a browser.

__Depends on [DevHttpD].__
```gradle
debugImplementation 'com.nextfaze.devfun:httpd-frontend:1.2.0'
```

Page is rather simple at the moment, but in the future it's somewhat intended (as a learning exercise) to create a React front end using
Kotlin or something.

![HTTP Server](https://github.com/NextFaze/dev-fun/raw/gh-pages/assets/images/httpd-auth-context.png)


### Stetho
Module [DevStetho] allows generated methods to be invoked from Chrome's Dev Tools JavaScript console.
```gradle
debugImplementation 'com.nextfaze.devfun:devfun-stetho:1.2.0'
```

Opening console will show available functions.
e.g. `Context_Enable_Account_Creation()`

_Extremely experimental and limited functionality._

![Stetho Integration](https://github.com/NextFaze/dev-fun/raw/gh-pages/assets/images/stetho-auth.png)

 */
object Components

/** Here to ensure the `.call` extension function stays in the import list (Kotlin IDE bug). */
@Suppress("unused")
private val dummy = (Any() as FunctionItem).call()
