[gh-pages](../../index.md) / [com.nextfaze.devfun.inject](../index.md) / [CapturingInstanceProvider](index.md) / [get](.)

# get

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> get(clazz: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)`<out T>): T?` [(source)](https://github.com/NextFaze/dev-fun/tree/master/devfun-annotations/src/main/java/com/nextfaze/devfun/inject/InstanceProvider.kt#L79)

Overrides [InstanceProvider.get](../-instance-provider/get.md)

Try to get an instance of some [clazz](get.md#com.nextfaze.devfun.inject.CapturingInstanceProvider$get(kotlin.reflect.KClass((com.nextfaze.devfun.inject.CapturingInstanceProvider.get.T)))/clazz).

**Return**
An instance of [clazz](get.md#com.nextfaze.devfun.inject.CapturingInstanceProvider$get(kotlin.reflect.KClass((com.nextfaze.devfun.inject.CapturingInstanceProvider.get.T)))/clazz), or `null` if this provider can not handle the type

**See Also**

[RequiringInstanceProvider.get](../-requiring-instance-provider/get.md)

