package com.nextfaze.devfun.compiler

import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic
import javax.tools.StandardLocation

// Kapt1 uses <buildDir>/intermediates/classes/<buildType>/...
// Kapt2 uses <buildDir>/tmp/kotlin-classes/<buildType>/...
// Kapt3 uses <buildDir>/tmp/kapt3/classes/<buildType>/...
// /home/user/<path>/<to>/<project>/<app-name>/build/intermediates/classes/<build-type>/META-INF/services/com.nextfaze.devfun.generated.MenuDefinitions
// Path may also be changed using gradle project.buildDir = '...' (default build dir ends with /build/, so we don't include that in regex)
private val SERVICES_PATH_REGEX = Regex("(.*)(/intermediates/classes/|/tmp/kotlin-classes/|/tmp/kapt3/classes/)(.*)/META-INF/services/.*")

// Patterns for BuildConfig.java
private val MANIFEST_PACKAGE_REGEX = Regex("\n?package (.*);")
private val APPLICATION_ID_REGEX = Regex("String APPLICATION_ID = \"(.*)\";")
private val BUILD_TYPE_REGEX = Regex("String BUILD_TYPE = \"(.*)\";")
private val FLAVOR_REGEX = Regex("String FLAVOR = \"(.*)\";")

private data class BuildConfig(val manifestPackage: String,
                               val applicationId: String,
                               val buildType: String,
                               val flavor: String) {
    val variantDir = when {
        flavor.isEmpty() -> buildType
        buildType.isEmpty() -> flavor
        else -> "$flavor/$buildType"
    }

    override fun toString() = "BuildConfig(manifestPackage='$manifestPackage', applicationId='$applicationId', buildType='$buildType', flavor='$flavor', variantDir='$variantDir')"
}

internal class CompileContext(private val processingEnv: ProcessingEnvironment) {
    val pkg by lazy {
        // Use apt arg PACKAGE_OVERRIDE
        packageOverride?.let { return@lazy it }.also { note { "pkg=$it" } }

        // Get apt arg PACKAGE_ROOT
        val packageRoot = packageRoot

        // Otherwise generate it from BuildConfig fields
        StringBuilder().apply {
            fun appendPart(part: String) {
                if (part.isNotBlank()) {
                    if (this.isNotEmpty()) {
                        append(".")
                    }
                    append(part)
                }
            }
            if (packageRoot == null) {
                appendPart(buildConfig.applicationId)
                appendPart(buildConfig.buildType)
                appendPart(buildConfig.flavor)
            } else {
                appendPart(packageRoot)
            }
            appendPart(packageSuffix)
        }.toString().also {
            note { "pkg=$it" }
        }
    }

    private val servicesPath by lazy {
        val resource = processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "", "$META_INF_SERVICES/services.tmp")
        try {
            File(resource.toUri()).canonicalPath
        } finally {
            resource.delete()
        }
    }

    private val resourcePathMatch by lazy {
        SERVICES_PATH_REGEX.matchEntire(servicesPath) ?: run {
            error("Failed to match resources path from $servicesPath")
            throw BuildContextException("Failed to locate active BuildConfig.java")
        }
    }

    private val buildDir by lazy {
        File(resourcePathMatch.groupValues[1]).also {
            if (!it.exists()) {
                error("buildDir $it does not exist!")
                throw BuildContextException("Failed to locate active BuildConfig.java - buildDir $it does not exist!")
            }
        }
    }
    private val buildVariant by lazy { resourcePathMatch.groupValues[3] }

    private val buildConfig by lazy {
        val buildConfigsDir = File(buildDir, "generated/source/buildConfig/")
        if (!buildConfigsDir.exists()) {
            error("Build configs directory does not exist at $buildConfigsDir (buildDir=$buildDir, servicesPath=$servicesPath)")
            throw BuildContextException("Failed to locate active BuildConfig.java")
        }

        val buildConfigFiles = buildConfigsDir.filterRecursively { it.name == "BuildConfig.java" }.filter { !it.canonicalPath.contains("androidTest") }
        if (buildConfigFiles.isEmpty()) {
            error("Failed to find any BuildConfig.java files int buildConfig directory $buildConfigsDir (buildDir=$buildDir, servicesPath=$servicesPath)")
            throw BuildContextException("Failed to locate active BuildConfig.java")
        }

        val buildConfigs = buildConfigFiles.map {
            val text = it.readText()
            BuildConfig(
                    manifestPackage = MANIFEST_PACKAGE_REGEX.find(text)?.groupValues?.getOrNull(1) ?: "",
                    applicationId = APPLICATION_ID_REGEX.find(text)?.groupValues?.getOrNull(1) ?: "",
                    buildType = BUILD_TYPE_REGEX.find(text)?.groupValues?.getOrNull(1) ?: "",
                    flavor = FLAVOR_REGEX.find(text)?.groupValues?.getOrNull(1) ?: "")
        }

        buildConfigs.singleOrNull {
            it.variantDir.equals(buildVariant, true) || it.variantDir.replace(File.separator, "").equals(buildVariant, true)
        } ?: run failedToMatch@ {
            error("Failed to match single build variant '$buildVariant' to any buildConfigs: $buildConfigs")
            throw BuildContextException("Failed to locate active BuildConfig.java")
        }
    }

    private val packageRoot by lazy { processingEnv.options[PACKAGE_ROOT]?.apply { trim() } }
    private val packageSuffix by lazy { (processingEnv.options[PACKAGE_SUFFIX] ?: PACKAGE_SUFFIX_DEFAULT).trim() }
    private val packageOverride by lazy { processingEnv.options[PACKAGE_OVERRIDE]?.trim() }

    private val isDebugVerbose by lazy { processingEnv.options[FLAG_DEBUG_VERBOSE]?.toBoolean() ?: false }

    private fun error(message: String) = processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
    private fun note(condition: Boolean = isDebugVerbose, body: () -> String) = runIf(condition) { processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, body()) }
}

private class BuildContextException(message: String?) : Throwable(message)
