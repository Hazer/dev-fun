@file:Suppress("UNUSED_PARAMETER", "unused", "PackageName", "ClassName")

package tested.developer_reference

import com.nextfaze.devfun.annotations.DeveloperAnnotation
import com.nextfaze.devfun.core.*
import com.nextfaze.devfun.test.expectArrayOf
import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass
import kotlin.test.expect

annotation class CustomPropertiesClassTypes

@Target(FUNCTION)
@Retention(SOURCE)
@DeveloperAnnotation(developerReference = true)
annotation class HasKClasses(
    val clazz: KClass<*>,
    val primitiveClass: KClass<*>,
    val typedClazz: KClass<*>,
    val nestedTypedStarClass: KClass<*>,
    val nestedTypedExplicitClass: KClass<*>,
    val privateClass: KClass<*>,
    val packagePrivateClass: KClass<*>,
    val privateTypedClass: KClass<*>,
    val packagePrivateTypedClass: KClass<*>,
    val multipleTypedClass: KClass<*>
)

@Target(FUNCTION)
@Retention(SOURCE)
@DeveloperAnnotation(developerReference = true)
internal annotation class HasKClassesBounded(
    val numberType1: KClass<out Number>,
    val numberTypeInt: KClass<out Number> = Int::class,
    val numberType3: KClass<in Number>,
    // TODO these don't work due to stubs compile issue >> error: incompatible types: Class<List> cannot be converted to Class<? extends List<?>>
//    val listType1: KClass<out List<*>>,
//    val listTypeDefault: KClass<out List<*>> = List::class,
    val listType2: KClass<in List<*>> = List::class,
    val packagePrivateBounded: KClass<PackagePrivateType>
)

@Target(FUNCTION)
@Retention(SOURCE)
@DeveloperAnnotation(developerReference = true)
annotation class HasKClassesOfArrayTypes(
    val booleanArray: KClass<*>,
    val byteArray: KClass<*>,
    val shortArray: KClass<*>,
    val intArray: KClass<*>,
    val longArray: KClass<*>,
    val charArray: KClass<*>,
    val floatArray: KClass<*>,
    val doubleArray: KClass<*>
)

@Target(FUNCTION)
@Retention(SOURCE)
@DeveloperAnnotation(developerReference = true)
annotation class HasKClassesWithDefaults(
    val defaultClass: KClass<*> = Any::class,
    val defaultPrimitiveClass: KClass<*> = BooleanArray::class,
    val defaultTypedClazz: KClass<*> = MyInterface::class
)

@Target(FUNCTION)
@Retention(SOURCE)
@DeveloperAnnotation(developerReference = true)
annotation class HasKClassArraysWithDefaults(
    val defaultClasses: Array<KClass<*>> = [Any::class, Int::class, BooleanArray::class],
    val optionalClasses: Array<KClass<*>> = [Boolean::class],
    val classes: Array<KClass<*>>
)

private class PrivateClass
class TypedClass<T : ReferenceDefinition>

interface MyInterface<T : FunctionTransformer>
class NestedTypedStarClass<T : MyInterface<*>>
class NestedTypedExplicitTypedClass<T : MyInterface<SingleFunctionTransformer>>

private class PrivateTypedClass<T : FunctionTransformer>
internal class PackagePrivateTypedClass<T : PackagePrivateType>

class MultipleTypedClass<T : MyInterface<*>, U : TypedClass<*>>

class cpct_SomeClass {
    @HasKClasses(
        clazz = cpct_SomeClass::class,
        primitiveClass = Byte::class,
        typedClazz = TypedClass::class,
        nestedTypedStarClass = NestedTypedStarClass::class,
        nestedTypedExplicitClass = NestedTypedExplicitTypedClass::class,
        privateClass = PrivateClass::class,
        packagePrivateClass = PackagePrivateType::class,
        privateTypedClass = PrivateTypedClass::class,
        packagePrivateTypedClass = PackagePrivateTypedClass::class,
        multipleTypedClass = MultipleTypedClass::class
    )
    fun testClassProperties(ref: MethodReference) {
        val properties = ref.getProperties<HasKClassesProperties>()
        expect(cpct_SomeClass::class) { properties.clazz }
        expect(Byte::class) { properties.primitiveClass }
        expect(TypedClass::class) { properties.typedClazz }
        expect(NestedTypedStarClass::class) { properties.nestedTypedStarClass }
        expect(NestedTypedExplicitTypedClass::class) { properties.nestedTypedExplicitClass }
        expect(PrivateClass::class) { properties.privateClass }
        expect(PackagePrivateType::class) { properties.packagePrivateClass }
        expect(PrivateTypedClass::class) { properties.privateTypedClass }
        expect(PackagePrivateTypedClass::class) { properties.packagePrivateTypedClass }
        expect(MultipleTypedClass::class) { properties.multipleTypedClass }
    }

    @HasKClassesBounded(
        numberType1 = Short::class,
        numberType3 = Number::class,
//        listType1 = List::class,
        listType2 = Collection::class,
        packagePrivateBounded = PackagePrivateType::class
    )
    fun testBoundedKClasses(ref: MethodReference) {
        val properties = ref.getProperties<HasKClassesBoundedProperties>()
        expect(Short::class) { properties.numberType1 }
        expect(Int::class) { properties.numberTypeInt }
        expect(Number::class) { properties.numberType3 }
//        expect(List::class) { properties.listType1 }
//        expect(List::class) { properties.listTypeDefault }
        expect(Collection::class) { properties.listType2 }
        expect(PackagePrivateType::class) { properties.packagePrivateBounded }
    }

    @HasKClassesOfArrayTypes(
        booleanArray = BooleanArray::class,
        byteArray = ByteArray::class,
        shortArray = ShortArray::class,
        intArray = IntArray::class,
        longArray = LongArray::class,
        charArray = CharArray::class,
        floatArray = FloatArray::class,
        doubleArray = DoubleArray::class
    )
    fun testArrayClassProperties(ref: MethodReference) {
        val properties = ref.getProperties<HasKClassesOfArrayTypesProperties>()
        expect(BooleanArray::class) { properties.booleanArray }
        expect(ByteArray::class) { properties.byteArray }
        expect(ShortArray::class) { properties.shortArray }
        expect(IntArray::class) { properties.intArray }
        expect(LongArray::class) { properties.longArray }
        expect(CharArray::class) { properties.charArray }
        expect(FloatArray::class) { properties.floatArray }
        expect(DoubleArray::class) { properties.doubleArray }
    }

    @HasKClassesWithDefaults
    fun testSimpleTypesWithDefaults(ref: MethodReference) {
        val properties = ref.getProperties<HasKClassesWithDefaultsProperties>()
        expect(Any::class) { properties.defaultClass }
        expect(BooleanArray::class) { properties.defaultPrimitiveClass }
        expect(MyInterface::class) { properties.defaultTypedClazz }
    }

    @HasKClassArraysWithDefaults(
        optionalClasses = [Int::class, cpct_SomeClass::class, ByteArray::class, PrivateClass::class, NestedTypedExplicitTypedClass::class],
        classes = [
            HasKClassArraysWithDefaults::class,
            NestedTypedStarClass::class,
            PackagePrivateTypedClass::class,
            Array<Boolean>::class,
            Array<HasKClassesWithDefaults>::class,
            Array<PrivateClass>::class,
            Array<PackagePrivateType>::class
        ]
    )
    fun testClassArrayPropertiesWithDefaults(ref: MethodReference) {
        val properties = ref.getProperties<HasKClassArraysWithDefaultsProperties>()
        expectArrayOf(
            Int::class,
            cpct_SomeClass::class,
            ByteArray::class,
            PrivateClass::class,
            NestedTypedExplicitTypedClass::class
        ) { properties.optionalClasses }
        expectArrayOf(
            HasKClassArraysWithDefaults::class,
            NestedTypedStarClass::class,
            PackagePrivateTypedClass::class,
            Array<Boolean>::class,
            Array<HasKClassesWithDefaults>::class,
            Array<PrivateClass>::class,
            Array<PackagePrivateType>::class
        ) { properties.classes }
    }
}
