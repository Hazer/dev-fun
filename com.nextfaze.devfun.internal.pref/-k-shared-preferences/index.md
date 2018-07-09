[gh-pages](../../index.md) / [com.nextfaze.devfun.internal.pref](../index.md) / [KSharedPreferences](./index.md)

# KSharedPreferences

`class KSharedPreferences` [(source)](https://github.com/NextFaze/dev-fun/tree/master/devfun-internal/src/main/java/com/nextfaze/devfun/internal/pref/SharedPreferences.kt#L27)

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `KSharedPreferences(preferences: `[`SharedPreferences`](https://developer.android.com/reference/android/content/SharedPreferences.html)`)` |

### Functions

| Name | Summary |
|---|---|
| [clear](clear.md) | `fun clear(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [get](get.md) | `operator fun get(key: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, default: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`KPreference`](../-k-preference/index.md)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>`operator fun get(key: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, default: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`KPreference`](../-k-preference/index.md)`<`[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`>`<br>`operator fun get(key: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, default: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`, onChange: `[`OnChange`](../-on-change.md)`<`[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`>? = null): `[`KPreference`](../-k-preference/index.md)`<`[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`>`<br>`operator fun get(key: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, default: `[`Float`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)`): `[`KPreference`](../-k-preference/index.md)`<`[`Float`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)`>`<br>`operator fun get(key: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, default: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`, onChange: `[`OnChange`](../-on-change.md)`<`[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`>? = null): `[`KPreference`](../-k-preference/index.md)`<`[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`>`<br>`operator fun <E : `[`Enum`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)`<`[`E`](get.md#E)`>> get(key: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, default: `[`E`](get.md#E)`, onChange: `[`OnChange`](../-on-change.md)`<`[`E`](get.md#E)`>? = null): `[`KPreference`](../-k-preference/index.md)`<`[`E`](get.md#E)`>`<br>`operator fun get(key: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, default: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`? = null): `[`KNullablePreference`](../-k-nullable-preference.md)`<`[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`>` |

### Companion Object Functions

| Name | Summary |
|---|---|
| [named](named.md) | `fun named(context: `[`Context`](https://developer.android.com/reference/android/content/Context.html)`, name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`KSharedPreferences`](./index.md) |
