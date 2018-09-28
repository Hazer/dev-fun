import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

@Suppress("unused")
fun Project.registerDependencies() {
    apply { from(rootProject.file("versions.gradle.kts")) }

    val kotlinVersion: String by extra
    val dokkaVersion: String by extra
    val agpVersion: String by extra

    Dependency.kotlinVersion = kotlinVersion
    Dependency.dokkaVersion = dokkaVersion
    Dependency.agpVersion = agpVersion
}

@Suppress("unused", "MemberVisibilityCanBePrivate")
object Dependency {
    lateinit var agpVersion: String
    val androidPlugin by lazy { "com.android.tools.build:gradle:$agpVersion" }

    lateinit var kotlinVersion: String
    fun kotlinPlugin(version: String = kotlinVersion) = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
    val kotlinPlugin by lazy { kotlinPlugin() }
    fun kotlinPluginApi(version: String = kotlinVersion) = "org.jetbrains.kotlin:kotlin-gradle-plugin-api:$version"
    val kotlinPluginApi by lazy { kotlinPluginApi() }
    fun kotlinStdLib(version: String = kotlinVersion) = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$version"
    val kotlinStdLib by lazy { kotlinStdLib() }
    val kotlinReflect by lazy { "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion" }
    val kotlinTest by lazy { "org.jetbrains.kotlin:kotlin-test:$kotlinVersion" }
    val kotlinAndroidExtensions by lazy { "org.jetbrains.kotlin:kotlin-android-extensions:$kotlinVersion" }

    const val kotlinCoroutinesVersion = "0.27.0"
    const val kotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion"
    const val kotlinCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinCoroutinesVersion"

    private const val supportVersion = "28.0.0"
    const val supportRecyclerView = "com.android.support:recyclerview-v7:$supportVersion"
    const val supportAppCompat = "com.android.support:appcompat-v7:$supportVersion"
    const val supportDesign = "com.android.support:design:$supportVersion"
    const val supportAnnotations = "com.android.support:support-annotations:$supportVersion"
    const val supportConstraintLayout = "androidx.constraintlayout:constraintlayout:1.1.3"
    const val androidX = "androidx.core:core-ktx:1.0.0"

    const val javaxInject = "javax.inject:javax.inject:1"

    const val daggerVersion = "2.17"
    fun dagger(version: String = daggerVersion) = "com.google.dagger:dagger:$version"
    fun daggerCompiler(version: String = daggerVersion) = "com.google.dagger:dagger-compiler:$version"
    val dagger = dagger()
    val daggerCompiler = daggerCompiler()
    const val daggerAnnotations = "org.glassfish:javax.annotation:10.0-b28"

    const val slf4jApi = "org.slf4j:slf4j-api:1.7.25"

    const val autoService = "com.google.auto.service:auto-service:1.0-rc4"

    private const val stethoVersion = "1.5.0"
    const val stetho = "com.facebook.stetho:stetho:$stethoVersion"
    const val stethoJsRhino = "com.facebook.stetho:stetho-js-rhino:$stethoVersion"

    const val glide = "com.github.bumptech.glide:glide:4.8.0"

    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:1.6.1"

    lateinit var dokkaVersion: String
    val dokkaFatJar by lazy { "org.jetbrains.dokka:dokka-fatjar:$dokkaVersion" }
}
