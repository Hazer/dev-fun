package com.nextfaze.devfun.gradle.plugin

import com.android.build.gradle.BaseExtension
import com.google.auto.service.AutoService
import com.nextfaze.devfun.compiler.*
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.internal.KaptVariantData
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * The DevFun Kotlin Gradle plugin. Configures the KAPT options.
 *
 * Attempts to automatically determine the application package and build variant.
 * Also passes though the script configuration options.
 *
 * @see DevFunGradlePlugin
 * @see DevFunExtension
 */
@AutoService(KotlinGradleSubplugin::class)
class DevFunKotlinGradlePlugin : KotlinGradleSubplugin<AbstractCompile> {
    /** The Gradle sub-plugin compiler plugin ID `com.nextfaze.devfun`. */
    override fun getCompilerPluginId() = DEVFUN_GROUP_NAME

    /** The Gradle sub-plugin artifact details. */
    override fun getPluginArtifact() = SubpluginArtifact(DEVFUN_GROUP_NAME, DEVFUN_ARTIFACT_NAME, DEVFUN_VERSION_NAME)

    /**
     * Determine if this plugin can be applied to this [project] and compile [task].
     *
     * For some reason the [apply] call never receives the first variant so most of the logic is performed in here instead.
     */
    override fun isApplicable(project: Project, task: AbstractCompile): Boolean {
        if (project.plugins.findPlugin(DevFunGradlePlugin::class.java) == null) return false
        if (task !is KotlinCompile) return false // we generate Kotlin code so definitely need this

        val androidExt = project.extensions.android ?: run {
            project.logger.error("Failed to find Android BaseExtension. Please ensure the devfun plugin is declared *after* the android plugin.")
            return false
        }
        val kotlinExt = project.extensions.kapt ?: run {
            project.logger.error("Failed to find KaptExtension. Please ensure the devfun plugin is declared *after* the kotlin-kapt plugin.")
            return false
        }
        val devFunExt = project.extensions.devFun ?: run {
            project.logger.error("Failed to find DevFunExtension. Please ensure the devfun plugin has been applied.")
            return false
        }

        val appPackage = getApplicationPackage(project, androidExt) ?: run {
            project.logger.warn("Unable to determine application package. Using DevFun package $DEVFUN_GROUP_NAME")
            DEVFUN_GROUP_NAME
        }
        val variant = run {
            val taskName = task.name
            kotlinCompileTaskNameRegex.find(taskName)?.groupValues?.getOrNull(1)?.decapitalize() ?: run {
                project.logger.warn("Failed to identify variant name from task name. Using 'main'")
                "main"
            }
        }

        kotlinExt.arguments {
            fun nonNullArg(name: String, value: String?) = value?.let { arg(name, it) }

            nonNullArg(APPLICATION_PACKAGE, appPackage)
            nonNullArg(APPLICATION_VARIANT, variant)

            nonNullArg(EXT_PACKAGE_SUFFIX, devFunExt.packageSuffix)
            nonNullArg(EXT_PACKAGE_ROOT, devFunExt.packageRoot)
            nonNullArg(EXT_PACKAGE_OVERRIDE, devFunExt.packageOverride)
        }

        return true
    }

    /** Apply this plugin to the project. _(is current a NOP due to never receiving first variant bug?)_ */
    override fun apply(
        project: Project,
        kotlinCompile: AbstractCompile,
        javaCompile: AbstractCompile?,
        variantData: Any?,
        androidProjectHandler: Any?,
        kotlinCompilation: KotlinCompilation?
    ): List<SubpluginOption> {
        //
        // TODO? Currently the kotlinExt value is not invoked for the first variant for some reason (which is typically the debug variant)
        //
        // Thus we're doing all our work in the isApplicable block instead (which is not ideal since we have to guess
        // the name from the task name rather than from the KaptVariantData object).
        //
        if (true) return emptyList()

        val androidExt = project.extensions.android ?: run {
            project.logger.error("Failed to find Android BaseExtension. Please ensure the devfun plugin is declared *after* the android plugin.")
            return emptyList()
        }
        val kotlinExt = project.extensions.kapt ?: run {
            project.logger.error("Failed to find KaptExtension. Please ensure the devfun plugin is declared *after* the kotlin-kapt plugin.")
            return emptyList()
        }
        val devFunExt = project.extensions.devFun ?: run {
            project.logger.error("Failed to find DevFunExtension. Please ensure the devfun plugin has been applied.")
            return emptyList()
        }

        val appPackage = getApplicationPackage(project, androidExt) ?: run {
            project.logger.warn("Unable to determine application package. Using DevFun package $DEVFUN_GROUP_NAME")
            DEVFUN_GROUP_NAME
        }
        val variant = if (variantData is KaptVariantData<*>) {
            variantData.name
        } else {
            val taskName = kotlinCompile.name
            project.logger.warn(
                """Variant data was not KaptVariantData<*> (variantData=$variantData).
                |Guessing variant name from kotlin compile $kotlinCompile""".trimMargin()
            )

            kotlinCompileTaskNameRegex.find(taskName)?.groupValues?.getOrNull(1)?.decapitalize() ?: run {
                project.logger.warn("Failed to identify variant name from task name. Using 'main'")
                "main"
            }
        }

        kotlinExt.arguments {
            fun nonNullArg(name: String, value: String?) = value?.let { arg(name, it) }

            nonNullArg(APPLICATION_PACKAGE, appPackage)
            nonNullArg(APPLICATION_VARIANT, variant)

            nonNullArg(EXT_PACKAGE_SUFFIX, devFunExt.packageSuffix)
            nonNullArg(EXT_PACKAGE_ROOT, devFunExt.packageRoot)
            nonNullArg(EXT_PACKAGE_OVERRIDE, devFunExt.packageOverride)
        }

        return emptyList()
    }
}

private const val DEVFUN_GROUP_NAME = "com.nextfaze.devfun"
private const val DEVFUN_ARTIFACT_NAME = "devfun-gradle-plugin"
private const val DEVFUN_VERSION_NAME = versionName

private val ExtensionContainer.android get() = findByType(BaseExtension::class.java)
private val ExtensionContainer.kapt get() = findByType(KaptExtension::class.java)
private val ExtensionContainer.devFun get() = findByType(DevFunExtension::class.java)

private val kotlinCompileTaskNameRegex = Regex("compile(.*)Kotlin")

private fun getApplicationPackage(project: Project, android: BaseExtension): String? {
    fun File.parseXml() = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this)

    val manifestFile = android.mainSourceSet.manifest.srcFile
    return try {
        manifestFile.parseXml().documentElement.getAttribute("package")
            .also {
                if (it.isNullOrBlank()) {
                    project.logger.warn("Application package name is not present in the manifest file (${manifestFile.absolutePath})")
                }
            }
    } catch (t: Throwable) {
        project.logger.warn("Failed to parse manifest file (${manifestFile.absolutePath})", t)
        null
    }
}

private val BaseExtension.mainSourceSet get() = sourceSets.getByName("main")
