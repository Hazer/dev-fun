import com.nextfaze.devfun.*
import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    id("com.android.application")
    id("kotlin-android-extensions")
    kotlin("android")
    kotlin("kapt")
    id("com.nextfaze.devfun")
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}

androidExtensions {
    isExperimental = true
}

android {
    compileSdkVersion(Android.compileSdkVersion)

    defaultConfig {
        applicationId = "com.nextfaze.devfun.demo"

        minSdkVersion(Android.minSdkVersion)
        targetSdkVersion(Android.targetSdkVersion)
        versionCode = Android.versionCode
        versionName = Android.versionName(project)
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        if (project.isSnapshot) {
            javaCompileOptions {
                annotationProcessorOptions {
                    argument("devfun.debug.verbose", "true")
                }
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = getBooleanProperty("demo.debug.minify.enabled", true) // enable to demonstrate/test
            proguardFiles("proguard-rules.pro")
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
        }
    }
}

dependencies {
    // Dev Fun
    kaptDebug(project(":devfun-compiler"))
    implementation(project(":devfun-annotations"))
    debugImplementation(project(":devfun-inject-dagger2"))
    debugImplementation(project(":devfun-menu"))
    debugImplementation(project(":devfun-httpd"))
    debugImplementation(project(":devfun-httpd-frontend"))

    // Kotlin
    implementation(Config.kotlinStdLib)
    implementation(Config.kotlinReflect)
    implementation(Config.kotlinCoroutines)
    implementation(Config.kotlinCoroutinesAndroid)

    // Support libs
    implementation(Config.supportAppCompat)
    implementation(Config.supportDesign)
    implementation(Config.supportConstraintLayout)
    implementation("com.android.support:multidex:1.0.3")

    // Logging - https://github.com/tony19/logback-android
    implementation(Config.slf4jApi)
    implementation("com.github.tony19:logback-android-core:1.1.1-6")
    implementation("com.github.tony19:logback-android-classic:1.1.1-6") {
        exclude(group = "com.google.android", module = "android")
    }

    // Dagger 2 - https://github.com/google/dagger
    val daggerVersion = getStringProperty("testDaggerVersion", Config.daggerVersion)
    kapt(Config.daggerCompiler(daggerVersion))
    implementation(Config.dagger(daggerVersion))
    compileOnly(Config.daggerAnnotations)

    // OkHttp - https://github.com/square/okhttp
    implementation("com.squareup.okhttp3:okhttp:3.10.0")

    // Joda Time - https://github.com/dlew/joda-time-android
    implementation("net.danlew:android.joda:2.9.9")

    // Stetho - https://github.com/facebook/stetho
    debugImplementation(Config.stetho)
    debugImplementation(Config.stethoJsRhino)
    debugImplementation(project(":devfun-stetho"))

    // Glide - https://github.com/bumptech/glide
    implementation(Config.glide)
    implementation("com.github.bumptech.glide:okhttp3-integration:4.6.1@aar")
    debugImplementation(project(":devfun-util-glide"))

    // Leak Canary - https://github.com/square/leakcanary
    debugImplementation(Config.leakCanary)
    debugImplementation(project(":devfun-util-leakcanary"))

    // RxJava: Reactive Extensions for the JVM - https://github.com/ReactiveX/RxJava
    implementation("io.reactivex.rxjava2:rxjava:2.1.14")

    // RxJava bindings for Android - https://github.com/ReactiveX/RxAndroid
    implementation("io.reactivex.rxjava2:rxandroid:2.0.2")

    // RxKotlin
    implementation("io.reactivex.rxjava2:rxkotlin:2.2.0")

    // Auto Disposable
    implementation("com.uber.autodispose:autodispose-kotlin:0.8.0")
    implementation("com.uber.autodispose:autodispose-android-kotlin:0.8.0")
    implementation("com.uber.autodispose:autodispose-android-archcomponents-kotlin:0.8.0")

    // Instrumentation tests
    androidTestImplementation(Config.kotlinTest)

    androidTestImplementation("androidx.test:rules:1.1.0-alpha3")
    androidTestImplementation("androidx.test:runner:1.1.0-alpha3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0-alpha3")
}

project.afterEvaluate {
    getTasksByName("connectedDebugAndroidTest", false).single().apply {
        doFirst {
            val testDaggerVersion = getStringProperty("testDaggerVersion", "")
            if (testDaggerVersion.isNotEmpty()) {
                println("testDaggerVersion=$testDaggerVersion")
            }
        }
    }
}
