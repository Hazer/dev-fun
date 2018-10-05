[gh-pages](../index.md) / [com.nextfaze.devfun.inject.dagger2](index.md) / [tryGetInstanceFromComponentReflection](./try-get-instance-from-component-reflection.md)

# tryGetInstanceFromComponentReflection

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> tryGetInstanceFromComponentReflection(component: `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`, clazz: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)`<`[`T`](try-get-instance-from-component-reflection.md#T)`>): `[`T`](try-get-instance-from-component-reflection.md#T)`?` [(source)](https://github.com/NextFaze/dev-fun/tree/master/devfun-inject-dagger2/src/main/java/com/nextfaze/devfun/inject/dagger2/Instances.kt#L143)

Helper function to be used on Dagger 2.x [Component](#) implementations.

Will traverse the component providers and modules for an instance type matching [clazz](try-get-instance-from-component-reflection.md#com.nextfaze.devfun.inject.dagger2$tryGetInstanceFromComponentReflection(kotlin.Any, kotlin.reflect.KClass((com.nextfaze.devfun.inject.dagger2.tryGetInstanceFromComponentReflection.T)))/clazz) - scoping is not considered.

You should use [tryGetInstanceFromComponentCache](try-get-instance-from-component-cache.md) before this method may create new instances instead of reusing
them due to scoping limitations. This can be avoided to some degree if your `@Scope` annotations are `@Retention(RUNTIME)`.

Rather then using this function directly you can use [tryGetInstanceFromComponent](try-get-instance-from-component.md) which tries this first then the reflection method.

Alternatively use `@Dagger2Component` on your functions/properties (or `@get:Dagger2Component` for property getters)
that return components to tell DevFun where to find them (they can be whatever/where ever; static, in your app class,
activity class, etc) - which will end up using this method anyway.

**See Also**

[Dagger2Component](../com.nextfaze.devfun.reference/-dagger2-component/index.md)

