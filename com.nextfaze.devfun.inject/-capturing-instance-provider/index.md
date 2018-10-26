[gh-pages](../../index.md) / [com.nextfaze.devfun.inject](../index.md) / [CapturingInstanceProvider](./index.md)

# CapturingInstanceProvider

`class CapturingInstanceProvider<out T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`InstanceProvider`](../-instance-provider/index.md) [(source)](https://github.com/NextFaze/dev-fun/tree/master/devfun-annotations/src/main/java/com/nextfaze/devfun/inject/InstanceProvider.kt#L93)

An instance provider that requests an instance of a class from a captured lambda.

Be aware of leaks! The lambda could implicitly hold a local `this` reference.

Be wary of using a `typealias` as a type - the resultant function "type" itself is used at compile time.
e.g.

``` kotlin
typealias MyStringAlias = () -> String?
val provider1 = captureInstance<MyStringAlias> { ... }

typealias MyOtherAlias = () -> Type?
// will be triggered for MyStringAlias and MyOtherAlias since behind the scenes they are both kotlin.Function0<T>
val provider2 = captureInstance<MyOtherAlias> { ... }
```

**See Also**

[captureInstance](../capture-instance.md)

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `CapturingInstanceProvider(instanceClass: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)`<`[`T`](index.md#T)`>, instance: () -> `[`T`](index.md#T)`?)`<br>An instance provider that requests an instance of a class from a captured lambda. |

### Functions

| Name | Summary |
|---|---|
| [get](get.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> get(clazz: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)`<out `[`T`](get.md#T)`>): `[`T`](get.md#T)`?`<br>Try to get an instance of some [clazz](../-instance-provider/get.md#com.nextfaze.devfun.inject.InstanceProvider$get(kotlin.reflect.KClass((com.nextfaze.devfun.inject.InstanceProvider.get.T)))/clazz). |
