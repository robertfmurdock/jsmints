package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import kotlin.test.Test

class TestingLibraryByRoleTest {

    @Test
    fun givenElementExistsCanGetByRole() = givenElementByRoleWorks(TestingLibraryBrowser::getByRole)

    @Test
    fun givenElementExistsCanFindByRole() = givenElementByRoleWorks(TestingLibraryBrowser::findByRole)

    @Test
    fun givenElementExistsCanQueryByRole() = givenElementByRoleWorks(TestingLibraryBrowser::queryByRole)

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
    fun givenNoElementExistsGetByRole() = givenNoElementByRoleWillFailAsExpected(TestingLibraryBrowser::getByRole)

    @Test
    fun givenNoElementExistsFindByRole() = givenNoElementByRoleWillFailAsExpected(TestingLibraryBrowser::findByRole)

    private fun givenNoElementByRoleWillFailAsExpected(
        query: suspend (role: String, options: RoleOptions) -> WebdriverElement?
    ) = testingLibrarySetup {
    } exercise {
        kotlin.runCatching { query("button", RoleOptions(name = "Not Awesome")) }
    } verify { result ->
        result.isFailure
            .assertIsEqualTo(true)
    }

    @Test
    fun givenNoElementExistsQueryByRole() = testingLibrarySetup(object {
        val browser = TestingLibraryBrowser
    }) {
    } exercise {
        browser.queryByRole("button", RoleOptions(name = "Not Awesome"))
    } verify { element ->
        element.isPresent().assertIsEqualTo(false)
        element.isDisplayed().assertIsEqualTo(false)
    }

    @Test
    fun givenMultipleElementExistsErrorsOnGetByRole() =
        givenMultipleElementsByRoleErrors(TestingLibraryBrowser::getByRole)

    @Test
    fun givenMultipleElementExistsErrorsOnFindByRole() =
        givenMultipleElementsByRoleErrors(TestingLibraryBrowser::findByRole)

    @Test
    fun givenMultipleElementExistsErrorsOnQueryByRole() =
        givenMultipleElementsByRoleErrors(TestingLibraryBrowser::queryByRole)

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
    fun givenMultipleElementExistsSucceedsOnGetAllByRole() =
        givenMultipleElementsByRoleSucceeds(TestingLibraryBrowser::getAllByRole)

    @Test
    fun givenMultipleElementExistsSucceedsOnFindAllByRole() =
        givenMultipleElementsByRoleSucceeds(TestingLibraryBrowser::findAllByRole)

    @Test
    fun givenMultipleElementExistsSucceedsOnQueryAllByRole() =
        givenMultipleElementsByRoleSucceeds(TestingLibraryBrowser::queryAllByRole)

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
    fun givenSingleElementExistsSucceedsOnGetAllByRole() =
        givenSingleElementsByRoleSucceeds(TestingLibraryBrowser::getAllByRole)

    @Test
    fun givenSingleElementExistsSucceedsOnFindAllByRole() =
        givenSingleElementsByRoleSucceeds(TestingLibraryBrowser::findAllByRole)

    @Test
    fun givenSingleElementExistsSucceedsOnQueryAllByRole() =
        givenSingleElementsByRoleSucceeds(TestingLibraryBrowser::queryAllByRole)

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
