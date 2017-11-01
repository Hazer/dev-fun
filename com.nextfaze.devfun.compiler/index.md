[gh-pages](../index.md) / [com.nextfaze.devfun.compiler](.)

## Package com.nextfaze.devfun.compiler

Annotation processor that handles [DeveloperFunction](https://nextfaze.github.io/dev-fun/com.nextfaze.devfun.annotations/-developer-function/)
 and [DeveloperCategory](https://nextfaze.github.io/dev-fun/com.nextfaze.devfun.annotations/-developer-category/) annotations.

### Types

| Name | Summary |
|---|---|
| [DevFunProcessor](-dev-fun-processor/index.md) | `class DevFunProcessor : AbstractProcessor`<br>Annotation processor for [DeveloperFunction](../com.nextfaze.devfun.annotations/-developer-function/index.md) and [DeveloperCategory](../com.nextfaze.devfun.annotations/-developer-category/index.md). |

### Properties

| Name | Summary |
|---|---|
| [FLAG_DEBUG_VERBOSE](-f-l-a-g_-d-e-b-u-g_-v-e-r-b-o-s-e.md) | `const val FLAG_DEBUG_VERBOSE: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Flag to enable additional compile/processing log output. *(default: `false`)* |
| [FLAG_USE_KOTLIN_REFLECTION](-f-l-a-g_-u-s-e_-k-o-t-l-i-n_-r-e-f-l-e-c-t-i-o-n.md) | `const val FLAG_USE_KOTLIN_REFLECTION: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Flag to enable Kotlin reflection to get method references. *(default: `false`)* **(experimental)** |
| [PACKAGE_OVERRIDE](-p-a-c-k-a-g-e_-o-v-e-r-r-i-d-e.md) | `const val PACKAGE_OVERRIDE: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Sets the package for the generated code. *(default: `<none>`)* |
| [PACKAGE_ROOT](-p-a-c-k-a-g-e_-r-o-o-t.md) | `const val PACKAGE_ROOT: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Sets the package root for the generated code. *(default: `<project package>`)* |
| [PACKAGE_SUFFIX](-p-a-c-k-a-g-e_-s-u-f-f-i-x.md) | `const val PACKAGE_SUFFIX: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Sets the package suffix for the generated code. *(default: `devfun_generated`)* |
| [PACKAGE_SUFFIX_DEFAULT](-p-a-c-k-a-g-e_-s-u-f-f-i-x_-d-e-f-a-u-l-t.md) | `const val PACKAGE_SUFFIX_DEFAULT: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Default package output suffix: `devfun_generated` |
