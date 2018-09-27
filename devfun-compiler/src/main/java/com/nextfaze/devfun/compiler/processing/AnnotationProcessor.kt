package com.nextfaze.devfun.compiler.processing

import com.nextfaze.devfun.compiler.*
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

internal interface Processor : WithElements {
    fun Element.toClass(
        kotlinClass: Boolean = true,
        isKtFile: Boolean = false,
        castIfNotPublic: KClass<*>? = null,
        vararg types: KClass<*>
    ) =
        asType().toClass(
            kotlinClass = kotlinClass,
            isKtFile = isKtFile,
            elements = elements,
            castIfNotPublic = castIfNotPublic,
            types = *types
        )

    fun TypeMirror.toClass(
        kotlinClass: Boolean = true,
        isKtFile: Boolean = false,
        castIfNotPublic: KClass<*>? = null,
        vararg types: KClass<*>
    ) =
        toClass(
            kotlinClass = kotlinClass,
            isKtFile = isKtFile,
            elements = elements,
            castIfNotPublic = castIfNotPublic,
            types = *types
        )

    // during normal gradle builds string types will be java.lang
    // during testing however they will be kotlin types
    val TypeMirror.isString get() = toString().let { it == "java.lang.String" || it == "kotlin.String" }

    // todo remove this once https://github.com/square/kotlinpoet/issues/439 resolved
    fun String.toLiteral(stringPreprocessor: StringPreprocessor, element: Element?) =
        stringPreprocessor.run(toKString(trimMargin = true), element)
}

internal interface AnnotationProcessor : Processor {
    val willGenerateSource: Boolean

    fun generateSource(): String

    fun processAnnotatedElement(annotatedElement: AnnotatedElement, env: RoundEnvironment)

    fun KCallable<*>.toPropertySpec(
        propName: String = name,
        propReturnType: TypeName = returnType.asTypeName()
    ) =
        PropertySpec.builder(
            propName,
            propReturnType,
            KModifier.OVERRIDE
        )
}

data class AnnotatedElement(
    val element: Element,
    val annotationElement: TypeElement,
    val asFunction: Boolean,
    val asCategory: Boolean,
    val asReference: Boolean
) {
    val annotation: AnnotationMirror = element.getAnnotation(annotationElement)!!
}
