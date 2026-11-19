package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.minassert.assertIsEqualTo
import org.w3c.dom.HTMLElement
import kotlin.test.Test

class ByRoleTest : ByRole by TestingLibraryReact.screen {

    @Test
    fun givenOneMatchGetByRoleReturnsMatch() = givenElementByRoleWorks(::getByRole)

    @Test
    fun givenOneMatchFindByRoleReturnsMatch() = givenElementByRoleWorks(::findByRole)

    @Test
    fun givenOneMatchQueryByRoleReturnsMatch() = givenElementByRoleWorks(::queryByRole)

    private fun givenElementByRoleWorks(query: suspend (role: String, options: RoleOptions) -> HTMLElement?) = testingLibrarySetup {
    } exercise {
        query("button", RoleOptions(name = "Press Me"))
    } verify { element ->
        element?.isConnected
            .assertIsEqualTo(true)
        element?.getAttribute("data-test-info")
            .assertIsEqualTo("pretty-cool")
    }

    @Test
    fun givenNoMatchGetByRoleThrows() = givenNoElementByRoleWillFailAsExpected(::getByRole)

    @Test
    fun givenNoMatchFindByRoleThrows() = givenNoElementByRoleWillFailAsExpected(::findByRole)

    private fun givenNoElementByRoleWillFailAsExpected(
        query: suspend (role: String, options: RoleOptions) -> HTMLElement?,
    ) = testingLibrarySetup {
    } exercise {
        runCatching { query("button", RoleOptions(name = "Not Awesome")) }
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
    fun givenNoMatchQueryByRoleReturnsNull() = testingLibrarySetup {
    } exercise {
        queryByRole("button", RoleOptions(name = "Not Awesome"))
    } verify { element ->
        element.assertIsEqualTo(null)
    }

    @Test
    fun givenMultipleMatchesGetByRoleThrows() = givenMultipleElementsByRoleErrors(::getByRole)

    @Test
    fun givenMultipleMatchesFindByRoleThrows() = givenMultipleElementsByRoleErrors(::findByRole)

    @Test
    fun givenMultipleMatchesQueryByRoleThrows() = givenMultipleElementsByRoleErrors(::queryByRole)

    private fun givenMultipleElementsByRoleErrors(query: suspend (role: String, options: RoleOptions) -> HTMLElement?) = testingLibrarySetup {
    } exercise {
        runCatching { query("button", RoleOptions(name = "Chill")) }
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

    private fun givenMultipleElementsByRoleSucceeds(query: suspend (role: String, options: RoleOptions) -> Array<HTMLElement>) = testingLibrarySetup {
    } exercise {
        query("button", RoleOptions(name = "Chill"))
    } verify { elements ->
        elements.map { it.getAttribute("data-test-info") }
            .assertIsEqualTo(listOf("pretty-cool", "very-cool", "extremely-cool"))
        elements.asList().forEach { it.isConnected.assertIsEqualTo(true) }
    }

    @Test
    fun givenOneMatchGetAllByRoleReturnsOnlyMatch() = givenSingleElementsByRoleSucceeds(::getAllByRole)

    @Test
    fun givenOneMatchFindAllByRoleReturnsOnlyMatch() = givenSingleElementsByRoleSucceeds(::findAllByRole)

    @Test
    fun givenOneMatchQueryAllByRoleReturnsOnlyMatch() = givenSingleElementsByRoleSucceeds(::queryAllByRole)

    private fun givenSingleElementsByRoleSucceeds(query: suspend (role: String, options: RoleOptions) -> Array<HTMLElement>) = testingLibrarySetup {
    } exercise {
        query("button", RoleOptions(name = "Press Me"))
    } verify { elements ->
        elements.map { it.getAttribute("data-test-info") }
            .assertIsEqualTo(listOf("pretty-cool"))
        elements.asList().forEach { it.isConnected.assertIsEqualTo(true) }
    }
}
