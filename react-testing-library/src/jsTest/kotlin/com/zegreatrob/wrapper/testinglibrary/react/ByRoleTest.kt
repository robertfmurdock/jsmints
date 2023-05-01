package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.minassert.assertIsEqualTo
import org.w3c.dom.HTMLElement
import kotlin.test.Test

class ByRoleTest : ByRole by TestingLibraryReact.screen {

    @Test
    fun givenElementExistsCanGetByRole() = givenElementByRoleWorks(::getByRole)

    @Test
    fun givenElementExistsCanFindByRole() = givenElementByRoleWorks(::findByRole)

    @Test
    fun givenElementExistsCanQueryByRole() = givenElementByRoleWorks(::queryByRole)

    private fun givenElementByRoleWorks(query: suspend (role: String, options: RoleOptions) -> HTMLElement?) =
        testingLibrarySetup {
        } exercise {
            query("button", RoleOptions(name = "Press Me", selected = true))
        } verify { element ->
            element?.isConnected
                .assertIsEqualTo(true)
            element?.getAttribute("data-test-info")
                .assertIsEqualTo("pretty-cool")
        }

    @Test
    fun givenNoElementExistsGetByRole() = givenNoElementByRoleWillFailAsExpected(::getByRole)

    @Test
    fun givenNoElementExistsFindByRole() = givenNoElementByRoleWillFailAsExpected(::findByRole)

    private fun givenNoElementByRoleWillFailAsExpected(
        query: suspend (role: String, options: RoleOptions) -> HTMLElement?,
    ) = testingLibrarySetup {
    } exercise {
        runCatching { query("button", RoleOptions(name = "Not Awesome", selected = true)) }
    } verify { result ->
        result.isFailure
            .assertIsEqualTo(true)
    }

    @Test
    fun givenNoElementExistsQueryByRole() = testingLibrarySetup {
    } exercise {
        queryByRole("button", RoleOptions(name = "Not Awesome", selected = true))
    } verify { element ->
        element.assertIsEqualTo(null)
    }

    @Test
    fun givenMultipleElementExistsErrorsOnGetByRole() = givenMultipleElementsByRoleErrors(::getByRole)

    @Test
    fun givenMultipleElementExistsErrorsOnFindByRole() = givenMultipleElementsByRoleErrors(::findByRole)

    @Test
    fun givenMultipleElementExistsErrorsOnQueryByRole() = givenMultipleElementsByRoleErrors(::queryByRole)

    private fun givenMultipleElementsByRoleErrors(query: suspend (role: String, options: RoleOptions) -> HTMLElement?) =
        testingLibrarySetup {
        } exercise {
            runCatching { query("button", RoleOptions(name = "Chill", selected = true)) }
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

    private fun givenMultipleElementsByRoleSucceeds(query: suspend (role: String, options: RoleOptions) -> Array<HTMLElement>) =
        testingLibrarySetup {
        } exercise {
            query("button", RoleOptions(name = "Chill", selected = true))
        } verify { elements ->
            elements.map { it.getAttribute("data-test-info") }
                .assertIsEqualTo(listOf("pretty-cool", "very-cool", "extremely-cool"))
            elements.asList().forEach { it.isConnected.assertIsEqualTo(true) }
        }

    @Test
    fun givenSingleElementExistsSucceedsOnGetAllByRole() = givenSingleElementsByRoleSucceeds(::getAllByRole)

    @Test
    fun givenSingleElementExistsSucceedsOnFindAllByRole() = givenSingleElementsByRoleSucceeds(::findAllByRole)

    @Test
    fun givenSingleElementExistsSucceedsOnQueryAllByRole() = givenSingleElementsByRoleSucceeds(::queryAllByRole)

    private fun givenSingleElementsByRoleSucceeds(query: suspend (role: String, options: RoleOptions) -> Array<HTMLElement>) =
        testingLibrarySetup {
        } exercise {
            query("button", RoleOptions(name = "Press Me", selected = true))
        } verify { elements ->
            elements.map { it.getAttribute("data-test-info") }
                .assertIsEqualTo(listOf("pretty-cool"))
            elements.asList().forEach { it.isConnected.assertIsEqualTo(true) }
        }
}
