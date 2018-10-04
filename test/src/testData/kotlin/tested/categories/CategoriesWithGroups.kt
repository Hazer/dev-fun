@file:Suppress("ClassName")

package tested.categories

import com.nextfaze.devfun.category.DeveloperCategory
import com.nextfaze.devfun.function.DeveloperFunction
import com.nextfaze.devfun.test.ExpectedCategoryName
import com.nextfaze.devfun.test.ExpectedItemGroup

annotation class CategoriesWithGroups

@DeveloperCategory("My Category", group = "Group Fun")
class cwg_SimpleClass {
    @DeveloperFunction
    fun someFun() =
        listOf(
            ExpectedCategoryName("My Category"),
            ExpectedItemGroup("Group Fun")
        )

    @DeveloperFunction(category = DeveloperCategory("Custom Category 1"))
    fun functionWithCustomCategory() =
        listOf(
            ExpectedCategoryName("Custom Category 1"),
            ExpectedItemGroup(null) // new category thus no group will be defined
        )

    @DeveloperFunction(category = DeveloperCategory("Custom Category 2", group = ""))
    fun functionWithCustomCategoryRemovingGroup() =
        listOf(
            ExpectedCategoryName("Custom Category 2"),
            ExpectedItemGroup(null)
        )

    @DeveloperFunction(category = DeveloperCategory(group = "I'm Special"))
    fun functionWithCustomGroup() =
        listOf(
            ExpectedCategoryName("My Category"),
            ExpectedItemGroup("I'm Special")
        )

    @DeveloperFunction(category = DeveloperCategory("Custom Category 3", group = "Custom Group"))
    fun functionWithCustomCategoryAndGroup() =
        listOf(
            ExpectedCategoryName("Custom Category 3"),
            ExpectedItemGroup("Custom Group")
        )
}
