package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import kotlin.test.Test

class ByRoleTest : ByRole by TestingLibraryBrowser {

    @Test
    fun givenOneMatchGetByRoleReturnsMatch() = givenElementByRoleWorks(::getByRole)

    @Test
    fun givenOneMatchFindByRoleReturnsMatch() = givenElementByRoleWorks(::findByRole)

    @Test
    fun givenOneMatchQueryByRoleReturnsMatch() = givenElementByRoleWorks(::queryByRole)

    private fun givenElementByRoleWorks(query: suspend (role: String, options: RoleOptions) -> WebdriverElement?) = testingLibrarySetup {
    } exercise {
        query("button", RoleOptions(name = "Press Me"))
    } verify { element ->
        element?.isDisplayed()
            .assertIsEqualTo(true)
        element?.attribute("data-test-info")
            .assertIsEqualTo("pretty-cool")
    }

    @Test
    fun givenNoMatchGetByRoleThrows() = givenNoElementByRoleWillFailAsExpected(::getByRole)

    @Test
    fun givenNoMatchFindByRoleThrows() = givenNoElementByRoleWillFailAsExpected(::findByRole)

    private fun givenNoElementByRoleWillFailAsExpected(
        query: suspend (role: String, options: RoleOptions) -> WebdriverElement?,
    ) = staticQueryFailureSetup {
    } exercise {
        kotlin.runCatching { query("button", RoleOptions(name = "Not Awesome")) }
    } verify { result ->
        result.isFailure
            .assertIsEqualTo(true)
        val message = result.exceptionOrNull()?.message.orEmpty()
        message.startsWith("Unable to find")
            .assertIsEqualTo(true, message)
        message.contains("button")
            .assertIsEqualTo(true, message)
        message.contains("Not Awesome")
            .assertIsEqualTo(true, message)
    }

    @Test
    fun givenNoMatchQueryByRoleReportsNoElement() = testingLibrarySetup {
    } exercise {
        queryByRole("button", RoleOptions(name = "Not Awesome"))
    } verify { element ->
        element.isPresent().assertIsEqualTo(false)
        element.isDisplayed().assertIsEqualTo(false)
    }

    @Test
    fun givenMultipleMatchesGetByRoleThrows() = givenMultipleElementsByRoleErrors(::getByRole)

    @Test
    fun givenMultipleMatchesFindByRoleThrows() = givenMultipleElementsByRoleErrors(::findByRole)

    @Test
    fun givenMultipleMatchesQueryByRoleThrows() = givenMultipleElementsByRoleErrors(::queryByRole)

    private fun givenMultipleElementsByRoleErrors(query: suspend (role: String, options: RoleOptions) -> WebdriverElement?) = staticQueryFailureSetup {
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
    fun givenMultipleMatchesGetAllByRoleReturnsAllMatches() = givenMultipleElementsByRoleSucceeds(::getAllByRole)

    @Test
    fun givenMultipleMatchesFindAllByRoleReturnsAllMatches() = givenMultipleElementsByRoleSucceeds(::findAllByRole)

    @Test
    fun givenMultipleMatchesQueryAllByRoleReturnsAllMatches() = givenMultipleElementsByRoleSucceeds(::queryAllByRole)

    private fun givenMultipleElementsByRoleSucceeds(query: suspend (role: String, options: RoleOptions) -> WebdriverElementArray) = testingLibrarySetup {
    } exercise {
        query("button", RoleOptions(name = "Chill"))
    } verify { elements ->
        elements.map { it.attribute("data-test-info") }
            .assertIsEqualTo(listOf("pretty-cool", "very-cool", "extremely-cool"))
        elements.asList().forEach { it.isDisplayed().assertIsEqualTo(true) }
    }

    @Test
    fun givenOneMatchGetAllByRoleReturnsOnlyMatch() = givenSingleElementsByRoleSucceeds(::getAllByRole)

    @Test
    fun givenOneMatchFindAllByRoleReturnsOnlyMatch() = givenSingleElementsByRoleSucceeds(::findAllByRole)

    @Test
    fun givenOneMatchQueryAllByRoleReturnsOnlyMatch() = givenSingleElementsByRoleSucceeds(::queryAllByRole)

    private fun givenSingleElementsByRoleSucceeds(query: suspend (role: String, options: RoleOptions) -> WebdriverElementArray) = testingLibrarySetup {
    } exercise {
        query("button", RoleOptions(name = "Press Me"))
    } verify { elements ->
        elements.map { it.attribute("data-test-info") }
            .assertIsEqualTo(listOf("pretty-cool"))
        elements.asList().forEach { it.isDisplayed().assertIsEqualTo(true) }
    }
}
