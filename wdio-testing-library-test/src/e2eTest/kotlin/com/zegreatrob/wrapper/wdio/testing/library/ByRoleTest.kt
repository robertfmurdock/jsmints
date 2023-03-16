package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import kotlin.test.Test

class ByRoleTest : ByRole by TestingLibraryBrowser {

    @Test
    fun givenElementExistsCanGetByRole() = givenElementByRoleWorks(::getByRole)

    @Test
    fun givenElementExistsCanFindByRole() = givenElementByRoleWorks(::findByRole)

    @Test
    fun givenElementExistsCanQueryByRole() = givenElementByRoleWorks(::queryByRole)

    private fun givenElementByRoleWorks(query: suspend (role: String, options: RoleOptions) -> WebdriverElement?) =
        testingLibrarySetup {
        } exercise {
            query("button", RoleOptions(name = "Press Me"))
        } verify { element ->
            element?.isDisplayed()
                .assertIsEqualTo(true)
            element?.attribute("data-test-info")
                .assertIsEqualTo("pretty-cool")
        }

    @Test
    fun givenNoElementExistsGetByRole() = givenNoElementByRoleWillFailAsExpected(::getByRole)

    @Test
    fun givenNoElementExistsFindByRole() = givenNoElementByRoleWillFailAsExpected(::findByRole)

    private fun givenNoElementByRoleWillFailAsExpected(
        query: suspend (role: String, options: RoleOptions) -> WebdriverElement?,
    ) = testingLibrarySetup {
    } exercise {
        kotlin.runCatching { query("button", RoleOptions(name = "Not Awesome")) }
    } verify { result ->
        result.isFailure
            .assertIsEqualTo(true)
    }

    @Test
    fun givenNoElementExistsQueryByRole() = testingLibrarySetup {
    } exercise {
        queryByRole("button", RoleOptions(name = "Not Awesome"))
    } verify { element ->
        element.isPresent().assertIsEqualTo(false)
        element.isDisplayed().assertIsEqualTo(false)
    }

    @Test
    fun givenMultipleElementExistsErrorsOnGetByRole() = givenMultipleElementsByRoleErrors(::getByRole)

    @Test
    fun givenMultipleElementExistsErrorsOnFindByRole() = givenMultipleElementsByRoleErrors(::findByRole)

    @Test
    fun givenMultipleElementExistsErrorsOnQueryByRole() = givenMultipleElementsByRoleErrors(::queryByRole)

    private fun givenMultipleElementsByRoleErrors(query: suspend (role: String, options: RoleOptions) -> WebdriverElement?) =
        testingLibrarySetup {
        } exercise {
            kotlin.runCatching { query("button", RoleOptions(name = "Chill"))?.waitToExist() }
        } verify { result ->
            result.isFailure
                .assertIsEqualTo(true)
            result.exceptionOrNull()?.message.apply {
                this?.startsWith("Found multiple elements with the role \"button\" and name \"Chill\"")
                    .assertIsEqualTo(true, "<$this>")
            }
        }

    @Test
    fun givenMultipleElementExistsSucceedsOnGetAllByRole() = givenMultipleElementsByRoleSucceeds(::getAllByRole)

    @Test
    fun givenMultipleElementExistsSucceedsOnFindAllByRole() = givenMultipleElementsByRoleSucceeds(::findAllByRole)

    @Test
    fun givenMultipleElementExistsSucceedsOnQueryAllByRole() = givenMultipleElementsByRoleSucceeds(::queryAllByRole)

    private fun givenMultipleElementsByRoleSucceeds(query: suspend (role: String, options: RoleOptions) -> WebdriverElementArray) =
        testingLibrarySetup {
        } exercise {
            query("button", RoleOptions(name = "Chill"))
        } verify { elements ->
            elements.map { it.attribute("data-test-info") }
                .assertIsEqualTo(listOf("pretty-cool", "very-cool", "extremely-cool"))
            elements.asList().forEach { it.isDisplayed().assertIsEqualTo(true) }
        }

    @Test
    fun givenSingleElementExistsSucceedsOnGetAllByRole() = givenSingleElementsByRoleSucceeds(::getAllByRole)

    @Test
    fun givenSingleElementExistsSucceedsOnFindAllByRole() = givenSingleElementsByRoleSucceeds(::findAllByRole)

    @Test
    fun givenSingleElementExistsSucceedsOnQueryAllByRole() = givenSingleElementsByRoleSucceeds(::queryAllByRole)

    private fun givenSingleElementsByRoleSucceeds(query: suspend (role: String, options: RoleOptions) -> WebdriverElementArray) =
        testingLibrarySetup {
        } exercise {
            query("button", RoleOptions(name = "Press Me"))
        } verify { elements ->
            elements.map { it.attribute("data-test-info") }
                .assertIsEqualTo(listOf("pretty-cool"))
            elements.asList().forEach { it.isDisplayed().assertIsEqualTo(true) }
        }
}
