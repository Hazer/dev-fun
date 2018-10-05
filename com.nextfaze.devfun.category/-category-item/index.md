[gh-pages](../../index.md) / [com.nextfaze.devfun.category](../index.md) / [CategoryItem](./index.md)

# CategoryItem

`interface CategoryItem` [(source)](https://github.com/NextFaze/dev-fun/tree/master/devfun-annotations/src/main/java/com/nextfaze/devfun/category/CategoryItem.kt#L11)

Items are derived from [CategoryDefinition](../-category-definition/index.md) at run-time during [FunctionDefinition](../../com.nextfaze.devfun.function/-function-definition/index.md) processing.

Undefined categories will be derived from a definition's enclosing class.

### Properties

| Name | Summary |
|---|---|
| [items](items.md) | `abstract val items: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`FunctionItem`](../../com.nextfaze.devfun.function/-function-item/index.md)`>`<br>List of items for this category. |
| [name](name.md) | `abstract val name: `[`CharSequence`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char-sequence/index.html)<br>Categories with the same name will be merged at runtime (case-sensitive). |
| [order](order.md) | `open val order: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Categories are ordered by [order](order.md) (top-&gt;bottom is lowest-&gt;highest), then alphabetically by [name](name.md). |

### Inheritors

| Name | Summary |
|---|---|
| [ExceptionCategoryItem](../../com.nextfaze.devfun.internal.exception/-exception-category-item/index.md) | `class ExceptionCategoryItem : `[`CategoryItem`](./index.md) |
