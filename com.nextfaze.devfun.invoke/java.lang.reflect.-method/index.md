[gh-pages](../../index.md) / [com.nextfaze.devfun.invoke](../index.md) / [java.lang.reflect.Method](./index.md)

### Extensions for java.lang.reflect.Method

| Name | Summary |
|---|---|
| [parameterInstances](parameter-instances.md) | `fun `[`Method`](https://developer.android.com/reference/java/lang/reflect/Method.html)`.parameterInstances(instanceProvider: `[`InstanceProvider`](../../com.nextfaze.devfun.inject/-instance-provider/index.md)` = devFun.instanceProviders, args: `[`FunctionArgs`](../../com.nextfaze.devfun.core/-function-args.md)` = null): `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`?>?`<br>Get the parameter instances for this method for invocation. |
| [receiverClass](receiver-class.md) | `val `[`Method`](https://developer.android.com/reference/java/lang/reflect/Method.html)`.receiverClass: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)`<*>`<br>Get the receiver class for this method. |
| [receiverClassForInvocation](receiver-class-for-invocation.md) | `val `[`Method`](https://developer.android.com/reference/java/lang/reflect/Method.html)`.receiverClassForInvocation: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)`<*>?`<br>Get the receiver class for this function definition if you intend to invoke it. That is, it will return `null` if the type isn't needed. |
| [receiverInstance](receiver-instance.md) | `fun `[`Method`](https://developer.android.com/reference/java/lang/reflect/Method.html)`.receiverInstance(instanceProvider: `[`InstanceProvider`](../../com.nextfaze.devfun.inject/-instance-provider/index.md)` = devFun.instanceProviders): `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`?`<br>Get the receiver instance for this method to be used for invocation. |
