[gh-pages](../../index.md) / [com.nextfaze.devfun.httpd](../index.md) / [DevHttpD](./index.md)

# DevHttpD

`class DevHttpD : `[`AbstractDevFunModule`](../../com.nextfaze.devfun.core/-abstract-dev-fun-module/index.md) [(source)](https://github.com/NextFaze/dev-fun/tree/master/devfun-httpd/src/main/java/com/nextfaze/devfun/httpd/HttpD.kt#L54)

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `DevHttpD()` |

### Properties

| Name | Summary |
|---|---|
| [nano](nano.md) | `lateinit var nano: `[`HttpDRouter`](../-http-d-router/index.md) |

### Inherited Properties

| Name | Summary |
|---|---|
| [context](../../com.nextfaze.devfun.core/-abstract-dev-fun-module/context.md) | `val context: `[`Context`](https://developer.android.com/reference/android/content/Context.html)<br>Convenience delegate to [DevFun.context](../../com.nextfaze.devfun.core/-dev-fun/context.md). |
| [devFun](../../com.nextfaze.devfun.core/-abstract-dev-fun-module/dev-fun.md) | `val devFun: `[`DevFun`](../../com.nextfaze.devfun.core/-dev-fun/index.md)<br>Reference to owning [DevFun](../../com.nextfaze.devfun.core/-dev-fun/index.md) instance. |

### Functions

| Name | Summary |
|---|---|
| [dispose](dispose.md) | `fun dispose(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Module cleanup. |
| [init](init.md) | `fun init(context: `[`Context`](https://developer.android.com/reference/android/content/Context.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called upon [initialize](../../com.nextfaze.devfun.core/-abstract-dev-fun-module/initialize.md). |

### Inherited Functions

| Name | Summary |
|---|---|
| [get](../../com.nextfaze.devfun.core/-abstract-dev-fun-module/get.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> get(): `[`T`](../../com.nextfaze.devfun.core/-abstract-dev-fun-module/get.md#T) |
| [initialize](../../com.nextfaze.devfun.core/-abstract-dev-fun-module/initialize.md) | `open fun initialize(devFun: `[`DevFun`](../../com.nextfaze.devfun.core/-dev-fun/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Module initialization. |
| [instanceOf](../../com.nextfaze.devfun.core/-abstract-dev-fun-module/instance-of.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> instanceOf(clazz: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)`<out `[`T`](../../com.nextfaze.devfun.core/-abstract-dev-fun-module/instance-of.md#T)`>): `[`T`](../../com.nextfaze.devfun.core/-abstract-dev-fun-module/instance-of.md#T) |
