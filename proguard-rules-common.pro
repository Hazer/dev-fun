# Keep classes that are loaded via Java ServiceLoader (reflection)
-keep class com.nextfaze.devfun.core.DevFunModule
-keep class com.nextfaze.devfun.generated.DevFunGenerated

-keep class com.nextfaze.defun.** extends com.nextfaze.devfun.core.DevFunModule
-keep class com.nextfaze.defun.** extends com.nextfaze.devfun.generated.DevFunGenerated


# Keep @DeveloperFunction methods
-keep class com.nextfaze.devfun.** {
    @com.nextfaze.devfun.annotations.DeveloperFunction *;
}


# Keep @DeveloperCategory classes
-keep @com.nextfaze.devfun.annotations.DeveloperCategory class com.nextfaze.devfun.**


# Don't rename KObject INSTANCE field
-keepclassmembernames class com.nextfaze.devfun.** {
    public static final ** INSTANCE;
}


# Keep @Constructable types and their constructor
-keep class com.nextfaze.devfun.inject.Constructable
-keep @com.nextfaze.devfun.inject.Constructable class com.nextfaze.devfun.** {
    <init>(...);
}


# Keep metadata information for annotation introspection (i.e. for @Constructable)
-keep class kotlin.Metadata { *; }


# Kotlin class `kotlin.Function0`.toString() fails as it uses BuiltInsLoaderImpl (loaded by ServiceLoader)
#
# Keep Kotlin reflect builtins names and classes loaded by ServiceLoader
-keepnames class kotlin.reflect.jvm.internal.impl.builtins.**
-keep class kotlin.reflect.jvm.internal.impl.builtins.** extends kotlin.reflect.jvm.internal.impl.builtins.BuiltInsLoader
