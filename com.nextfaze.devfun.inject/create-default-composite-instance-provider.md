[gh-pages](../index.md) / [com.nextfaze.devfun.inject](index.md) / [createDefaultCompositeInstanceProvider](./create-default-composite-instance-provider.md)

# createDefaultCompositeInstanceProvider

`fun createDefaultCompositeInstanceProvider(cacheLevel: `[`CacheLevel`](-cache-level/index.md)` = CacheLevel.AGGRESSIVE): `[`CompositeInstanceProvider`](-composite-instance-provider.md) [(source)](https://github.com/NextFaze/dev-fun/tree/master/devfun/src/main/java/com/nextfaze/devfun/inject/InstanceProviders.kt#L31)

Creates an instance provider that delegates to other providers.

Checks in reverse order of added.
i.e. most recently added is checked first

**Internal**
Visible for testing - use at your own risk.

