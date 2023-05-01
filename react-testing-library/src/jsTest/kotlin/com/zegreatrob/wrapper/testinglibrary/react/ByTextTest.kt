package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.minassert.assertIsEqualTo
import org.w3c.dom.HTMLElement
import kotlin.test.Test

class ByTextTest : ByText by TestingLibraryReact.screen {

    @Test
    fun givenElementExistsCanGetByText() = givenElementByTextWorks(::getByText)

    @Test
    fun givenElementExistsCanFindByText() = givenElementByTextWorks(::findByText)

    @Test
    fun givenElementExistsCanQueryByText() = givenElementByTextWorks(::queryByText)

    private fun givenElementByTextWorks(query: suspend (text: String) -> HTMLElement?) = testingLibrarySetup {
    } exercise {
        query("Awesome")
    } verify { element ->
        element?.isConnected
            .assertIsEqualTo(true)
        element?.getAttribute("data-test-info")
            .assertIsEqualTo("pretty-cool")
    }

    @Test
    fun givenNoElementExistsGetByText() = givenNoElementByTextWillFailAsExpected(::getByText)

    @Test
    fun givenNoElementExistsFindByText() = givenNoElementByTextWillFailAsExpected(::findByText)

    private fun givenNoElementByTextWillFailAsExpected(
        query: suspend (text: String) -> HTMLElement?,
    ) = testingLibrarySetup {
    } exercise {
        runCatching { query("Not Awesome") }
    } verify { result ->
        result.isFailure
            .assertIsEqualTo(true)
        result.exceptionOrNull()?.message.apply {
            this?.startsWith(
                "Unable to find an element with the text: Not Awesome. " +
                    "This could be because the text is broken up by multiple elements. " +
                    "In this case, you can provide a function for your text matcher to " +
                    "make your matcher more flexible.",
            )
                .assertIsEqualTo(true, this)
        }
    }

    @Test
    fun givenNoElementExistsQueryByText() = testingLibrarySetup {
    } exercise {
        queryByText("Not Awesome")
    } verify { element ->
        element.assertIsEqualTo(null)
    }

    @Test
    fun givenMultipleElementExistsErrorsOnGetByText() = givenMultipleElementsByTextErrors(::getByText)

    @Test
    fun givenMultipleElementExistsErrorsOnFindByText() = givenMultipleElementsByTextErrors(::findByText)

    @Test
    fun givenMultipleElementExistsErrorsOnQueryByText() = givenMultipleElementsByTextErrors(::queryByText)

    private fun givenMultipleElementsByTextErrors(query: suspend (text: String) -> HTMLElement?) =
        testingLibrarySetup {
        } exercise {
            runCatching { query("Cool") }
        } verify { result ->
            result.isFailure
                .assertIsEqualTo(true)
            result.exceptionOrNull()?.message.apply {
                this?.startsWith("Found multiple elements with the text: Cool")
                    .assertIsEqualTo(true, "<$this>")
            }
        }

    @Test
    fun givenMultipleElementExistsSucceedsOnGetAllByText() = givenMultipleElementsByTextSucceeds(::getAllByText)

    @Test
    fun givenMultipleElementExistsSucceedsOnFindAllByText() = givenMultipleElementsByTextSucceeds(::findAllByText)

    @Test
    fun givenMultipleElementExistsSucceedsOnQueryAllByText() = givenMultipleElementsByTextSucceeds(::queryAllByText)

    private fun givenMultipleElementsByTextSucceeds(query: suspend (text: String) -> Array<HTMLElement>) =
        testingLibrarySetup {
        } exercise {
            query("Cool")
        } verify { elements ->
            elements.map { it.getAttribute("data-test-info") }
                .assertIsEqualTo(listOf("pretty-cool", "very-cool", "extremely-cool"))
            elements.asList().forEach { it.isConnected.assertIsEqualTo(true) }
        }

    @Test
    fun givenSingleElementExistsSucceedsOnGetAllByText() = givenSingleElementsByTextSucceeds(::getAllByText)

    @Test
    fun givenSingleElementExistsSucceedsOnFindAllByText() = givenSingleElementsByTextSucceeds(::findAllByText)

    @Test
    fun givenSingleElementExistsSucceedsOnQueryAllByText() = givenSingleElementsByTextSucceeds(::queryAllByText)

    private fun givenSingleElementsByTextSucceeds(query: suspend (text: String) -> Array<HTMLElement>) =
        testingLibrarySetup {
        } exercise {
            query("Awesome")
        } verify { elements ->
            elements.map { it.getAttribute("data-test-info") }
                .assertIsEqualTo(listOf("pretty-cool"))
            elements.asList().forEach { it.isConnected.assertIsEqualTo(true) }
        }
}
