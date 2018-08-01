package com.nextfaze.devfun.compiler

import javax.lang.model.element.*
import javax.lang.model.type.*
import javax.lang.model.util.Elements
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.jvm.internal.impl.name.FqName
import kotlin.reflect.jvm.internal.impl.platform.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.resolve.jvm.JvmPrimitiveType

internal inline val Element.isPublic get() = modifiers.contains(Modifier.PUBLIC)
internal inline val Element.isFinal get() = modifiers.contains(Modifier.FINAL)
internal inline val Element.isStatic get() = modifiers.contains(Modifier.STATIC)
internal inline val Element.isInterface get() = this.kind.isInterface
internal inline val Element.isProperty get() = isStatic && simpleName.endsWith("\$annotations")

internal inline val TypeMirror.isPrimitive get() = this.kind.isPrimitive
internal val TypeMirror.isClassPublic: Boolean
    get() {
        return when (this) {
            is PrimitiveType -> true
            is DeclaredType -> (asElement() as TypeElement).isClassPublic && typeArguments.all { it.isClassPublic } && asElement().enclosingElement.asType().isClassPublic
            is ExecutableType -> returnType.isClassPublic && parameterTypes.all { it.isClassPublic } && typeVariables.all { it.isClassPublic }
            is ArrayType -> componentType.isClassPublic
            is WildcardType -> extendsBound?.isClassPublic != false && superBound?.isClassPublic != false
            is TypeVariable -> upperBound?.isClassPublic != false && lowerBound?.isClassPublic != false
            is NoType -> true
            is NullType -> true
            else -> TODO("this=$this (${this::class})")
        }
    }

internal inline val TypeElement.isClassPublic: Boolean
    get() {
        var element = this
        while (true) {
            if (!element.isPublic) return false
            element = element.enclosingElement as? TypeElement ?: return true // hit package
        }
    }

internal inline val TypeElement.isClassKtFile
    get() = simpleName.endsWith("Kt") &&
            annotationMirrors.firstOrNull { it.annotationType.toString() == "kotlin.Metadata" }?.get<Int>("k") == 2

private inline val Element.typeElement get() = (asType() as? DeclaredType)?.asElement() as? TypeElement

internal inline val Element.isKObject: Boolean
    get() {
        val element = typeElement ?: return false
        val typeString by lazy(NONE) { asType().toString() }
        return (element.enclosedElements.any {
            it.simpleName.toString() == "INSTANCE" &&
                    it.isPublic && it.isFinal && it.isStatic &&
                    it.asType().toString() == typeString
        })
    }

internal inline operator fun <reified T : Any> AnnotationMirror.get(callable: KCallable<T>) =
    elementValues.filter { it.key.simpleName.toString() == callable.name }.values.singleOrNull()?.value as T?

internal inline operator fun <reified T : Any> AnnotationMirror.get(name: String) =
    elementValues.filter { it.key.simpleName.toString() == name }.values.singleOrNull()?.value as T?

internal inline operator fun <reified T : Annotation> AnnotationMirror.get(callable: KCallable<T>) =
    elementValues.filter { it.key.simpleName.toString() == callable.name }.values.singleOrNull()?.value as AnnotationMirror?

internal operator fun <K : KClass<*>> AnnotationMirror.get(
    callable: KCallable<K>,
    orDefault: (() -> DeclaredType?)? = null
): DeclaredType? {
    val entry = elementValues.filter { it.key.simpleName.toString() == callable.name }.entries.singleOrNull() ?: return orDefault?.invoke()
    return (entry.value.value ?: orDefault?.invoke()) as DeclaredType?
}

internal fun Name.stripInternal() = toString().substringBefore("\$")
internal fun CharSequence.escapeDollar() = this.toString().replace("\$", "\\\$")
internal fun String.toKString() = "\"\"\"${this.replace("\$", "\${'\$'}")}\"\"\""

internal inline val TypeElement.isCompanionObject
    get() = nestingKind.isNested && isStatic && simpleName.toString() == "Companion" && !isKObject

internal fun PrimitiveType.toKType() = JvmPrimitiveType.get(this.toString()).primitiveType
internal fun DeclaredType.toKType(): CharSequence {
    val name = (this.asElement() as QualifiedNameable).qualifiedName
    return JavaToKotlinClassMap.INSTANCE.mapJavaToKotlin(FqName(name.toString()))?.asSingleFqName()?.toString() ?: name
}

val TypeMirror.isPublic: Boolean
    get() = when (this) {
        is PrimitiveType -> true
        is ArrayType -> this.componentType.isPublic
        is TypeVariable -> this.upperBound.isPublic
        is DeclaredType -> this.asElement().isPublic && this.typeArguments.all { it.isPublic } && this.asElement().enclosingElement.asType().isPublic
        is WildcardType -> this.extendsBound?.isPublic ?: true && this.superBound?.isPublic ?: true
        is ExecutableType -> returnType.isPublic && parameterTypes.all { it.isPublic } && typeVariables.all { it.isPublic }
        is NoType -> true
        else -> throw NotImplementedError("TypeMirror.isPublic not implemented for this=$this (${this::class})")
    }

internal fun TypeMirror.toType(): CharSequence = when (this) {
    is PrimitiveType -> this.toKType().typeName.asString()
    is ArrayType -> when {
        this.componentType.isPrimitive -> (this.componentType as PrimitiveType).toKType().arrayTypeName.asString()
        else -> "Array<${this.componentType.toType()}>"
    }
    is DeclaredType -> when {
        this.typeArguments.isEmpty() -> this.toKType()
        else -> "${this.toKType()}<${this.typeArguments.joinToString { it.toType() }}>"
    }
    is WildcardType -> this.extendsBound?.toType() ?: this.superBound?.toType() ?: "*"
    is TypeVariable -> this.upperBound.toType()
    else -> throw NotImplementedError("TypeMirror.toType not implemented for this=$this (${this::class})")
}

internal fun TypeMirror.toClass(
    kotlinClass: Boolean = true,
    isKtFile: Boolean = false,
    elements: Elements,
    suffix: String = if (kotlinClass) "" else ".java",
    castIfNotPublic: KClass<*>? = null,
    vararg types: KClass<*>
): String = when {
    !isKtFile && isClassPublic -> when {
        this.kind.isPrimitive || this is ArrayType && this.componentType.isPrimitive -> "${this.toType()}::class$suffix"
        else -> "kClass<${this.toType()}>()$suffix"
    }
    else -> {
        when (this) {
            is DeclaredType -> {
                val type = asElement() as TypeElement
                val binaryName = elements.getBinaryName(type).escapeDollar()
                "Class.forName(\"$binaryName\")${if (kotlinClass) ".kotlin" else ""}" +
                        if (castIfNotPublic != null) " as ${castIfNotPublic.qualifiedName}${if (types.isNotEmpty()) "<${types.joinToString(", ") { it.qualifiedName!! }}>" else ""}" else ""
            }
            is ArrayType -> {
                "java.lang.reflect.Array.newInstance(${componentType.toClass(
                    kotlinClass = false,
                    isKtFile = isKtFile,
                    elements = elements,
                    suffix = suffix,
                    castIfNotPublic = castIfNotPublic,
                    types = *types
                )}, 0)::class"
            }
            else -> throw NotImplementedError("TypeMirror.toClass not implemented for this=$this (${this::class})")
        }
    }
}

internal fun TypeMirror.toCast(): String = when {
    isPublic -> when {
        this.kind.isPrimitive || this is ArrayType && this.componentType.isPrimitive -> " as ${this.toType()}"
        else -> " as ${this.toType()}"
    }
    else -> ""
}

//internal inline fun <reified T : Any> Element.getAnnotation(): AnnotationMirror? =
//    annotationMirrors.singleOrNull { it.annotationType.toString() == T::class.qualifiedName }

internal fun Element.getAnnotation(typeElement: TypeElement): AnnotationMirror? =
    annotationMirrors.singleOrNull { it.annotationType.toString() == typeElement.qualifiedName.toString() }

//internal fun Element.getAnnotation(typeElement: TypeElement): AnnotationMirror? =
//    annotationMirrors.singleOrNull { it.annotationType.asElement() == typeElement }
