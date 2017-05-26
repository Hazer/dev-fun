[gh-pages](../../index.md) / [com.nextfaze.devfun.core](../index.md) / [FunctionTransformer](index.md) / [apply](.)

# apply

`abstract fun apply(functionDefinition: `[`FunctionDefinition`](../-function-definition/index.md)`, categoryDefinition: `[`CategoryDefinition`](../-category-definition/index.md)`): `[`Collection`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-collection/index.html)`<`[`FunctionItem`](../-function-item/index.md)`>?` [(source)](https://github.com/NextFaze/dev-fun/tree/master/devfun-annotations/src/main/java/com/nextfaze/devfun/core/FunctionTransformer.kt#L70)

Transforms a [FunctionDefinition](../-function-definition/index.md) to one or more [FunctionItem](../-function-item/index.md).

Return `null` to ignore the item and allow another transformer to process it.

Returning anything but `null` will remove the item from other transformers.

