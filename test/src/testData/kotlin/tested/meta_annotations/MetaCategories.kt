@file:Suppress("unused", "ClassName", "PackageName")

package tested.meta_annotations

import com.nextfaze.devfun.DeveloperAnnotation
import com.nextfaze.devfun.category.CategoryItem
import com.nextfaze.devfun.category.DeveloperCategory
import com.nextfaze.devfun.function.DeveloperFunction
import com.nextfaze.devfun.function.FunctionDefinition
import com.nextfaze.devfun.function.FunctionItem
import com.nextfaze.devfun.test.NOPCategoryItem
import com.nextfaze.devfun.test.NOPFunctionItem
import kotlin.test.expect

annotation class MetaCategories

@DeveloperAnnotation(developerCategory = true)
annotation class CustomCategory(
    val value: String = "My Custom Category",
    val order: Int = 5_000
)

@DeveloperAnnotation(developerCategory = true)
annotation class CategoryContext(
    val value: String = "Context",
    val order: Int = -10_000
)

@DeveloperAnnotation(developerCategory = true)
annotation class MetaCategory(
    val value: String = "",
    val group: String = "",
    val order: Int = 0
)

@CustomCategory
class mc_SimpleClass {
    @DeveloperFunction
    fun testCustomCategory(functionItem: FunctionItem, categoryItem: CategoryItem) {
        if (functionItem === NOPFunctionItem || categoryItem === NOPCategoryItem) return

        expect("My Custom Category") { functionItem.category.name }
    }
}

@CategoryContext
class mc_ContextClass {
    @DeveloperFunction
    fun testFunctionInContextCat(functionItem: FunctionItem, categoryItem: CategoryItem) {
        if (functionItem === NOPFunctionItem || categoryItem === NOPCategoryItem) return

        expect("Context") { functionItem.category.name }
        expect(-10_000) { functionItem.category.order }
    }

    @DeveloperFunction(category = DeveloperCategory("Different Category"))
    fun testFunctionInContextCatClassWithCustomCat(functionDefinition: FunctionDefinition) {
        expect("Different Category") { functionDefinition.category?.name }
    }

    @DeveloperFunction(category = DeveloperCategory("Different Category"))
    fun testFunctionInContextCatClassWithCustomCat(functionItem: FunctionItem) {
        if (functionItem === NOPFunctionItem) return

        expect("Different Category") { functionItem.category.name }
        expect(null) { functionItem.category.order }
    }

    @DeveloperFunction(category = DeveloperCategory("My Custom Category"))
    fun testFunctionInContextCatClassWithAnotherCat(functionDefinition: FunctionDefinition) {
        expect("My Custom Category") { functionDefinition.category?.name }
    }

    @DeveloperFunction(category = DeveloperCategory("My Custom Category"))
    fun testFunctionInContextCatClassWithAnotherCat(functionItem: FunctionItem) {
        if (functionItem === NOPFunctionItem) return

        expect("My Custom Category") { functionItem.category.name }
        expect(5_000) { functionItem.category.order }
    }
}

@MetaCategory
class mc_MetaCategory {
    @DeveloperFunction
    fun testAllDefaults(functionItem: FunctionItem, categoryItem: CategoryItem) {
        if (functionItem === NOPFunctionItem || categoryItem === NOPCategoryItem) return

        expect("Mc Meta Category") { categoryItem.name }
        expect(null) { functionItem.group }
        expect(0) { categoryItem.order }
    }

    @DeveloperFunction(category = DeveloperCategory(group = "Function Group"))
    fun testFunctionDefinedGroup(functionItem: FunctionItem, categoryItem: CategoryItem) {
        if (functionItem === NOPFunctionItem || categoryItem === NOPCategoryItem) return

        expect("Mc Meta Category") { categoryItem.name }
        expect("Function Group") { functionItem.group }
        expect(0) { categoryItem.order }
    }

    @DeveloperFunction(category = DeveloperCategory("Function Defined Category", group = "Function Group", order = -9_876))
    fun testAllFunctionDefined(functionItem: FunctionItem, categoryItem: CategoryItem) {
        if (functionItem === NOPFunctionItem || categoryItem === NOPCategoryItem) return

        expect("Function Defined Category") { categoryItem.name }
        expect("Function Group") { functionItem.group }
        expect(-9_876) { categoryItem.order }
    }
}
