@file:Suppress("unused", "ClassName", "PackageName")

package tested.meta_annotations

import com.nextfaze.devfun.annotations.DeveloperCategory
import com.nextfaze.devfun.annotations.DeveloperFunction
import com.nextfaze.devfun.core.FunctionItem
import com.nextfaze.devfun.test.NOPFunctionItem
import kotlin.test.expect

annotation class MetaGroupingCategory


@DeveloperCategory("My Custom Grouping Category")
annotation class CustomGroupingCategory(val group: String = "")

@CustomGroupingCategory
class mgc_SimpleClass {
    @DeveloperFunction
    fun testFunctionInCustomGroupingCat(functionItem: FunctionItem) {
        if (functionItem === NOPFunctionItem) return

        expect("My Custom Grouping Category") { functionItem.category.name }
        expect(null) { functionItem.group }
    }
}

@CustomGroupingCategory("Group1")
class mgc_SimpleGroup1Class {
    @DeveloperFunction
    fun testFunctionInGroupedCustomGroupingCat(functionItem: FunctionItem) {
        if (functionItem === NOPFunctionItem) return

        expect("My Custom Grouping Category") { functionItem.category.name }
        expect("Group1") { functionItem.group }
    }
}

@CustomGroupingCategory("Group2")
class mgc_SimpleClassWithFunGroup {
    @DeveloperFunction(category = DeveloperCategory(group = "SpecialGroup"))
    fun testFunctionInGroupedCustomGroupingCat(functionItem: FunctionItem) {
        if (functionItem === NOPFunctionItem) return

        expect("My Custom Grouping Category") { functionItem.category.name }
        expect("SpecialGroup") { functionItem.group }
    }
}


@DeveloperCategory("My Custom Default Grouping Category")
annotation class CustomDefaultGroupingCategory(val group: String = "DefaultGroup")

@CustomDefaultGroupingCategory
class mgc_DefaultGroupingClass {
    @DeveloperFunction
    fun testFunctionInDefaultGrouping(functionItem: FunctionItem) {
        if (functionItem === NOPFunctionItem) return

        expect("My Custom Default Grouping Category") { functionItem.category.name }
        expect("DefaultGroup") { functionItem.group }
    }
}

@CustomDefaultGroupingCategory("Grouped")
class mgc_DefaultGroupingClassGrouped {
    @DeveloperFunction
    fun testDefaultGroupedAtClass(functionItem: FunctionItem) {
        if (functionItem === NOPFunctionItem) return

        expect("My Custom Default Grouping Category") { functionItem.category.name }
        expect("Grouped") { functionItem.group }
    }
}

@CustomDefaultGroupingCategory("Grouped")
class mgc_DefaultGroupingClassGroupAndFunctionGrouped {
    @DeveloperFunction(category = DeveloperCategory(group = "YAG"))
    fun testDefaultGroupedAtClassAndFunction(functionItem: FunctionItem) {
        if (functionItem === NOPFunctionItem) return

        expect("My Custom Default Grouping Category") { functionItem.category.name }
        expect("YAG") { functionItem.group }
    }
}
