package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.minassert.assertIsEqualTo
import org.w3c.dom.HTMLElement
import kotlin.test.Test

class ByTextTest : ByText by TestingLibraryReact.screen {

    @Test
    fun givenOneMatchGetByTextReturnsMatch() = givenElementByTextWorks(::getByText)

    @Test
    fun givenOneMatchFindByTextReturnsMatch() = givenElementByTextWorks(::findByText)

    @Test
    fun givenOneMatchQueryByTextReturnsMatch() = givenElementByTextWorks(::queryByText)

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
    fun givenNoMatchGetByTextThrows() = givenNoElementByTextWillFailAsExpected(::getByText)

    @Test
    fun givenNoMatchFindByTextThrows() = givenNoElementByTextWillFailAsExpected(::findByText)

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
    fun givenNoMatchQueryByTextReturnsNull() = testingLibrarySetup {
    } exercise {
        queryByText("Not Awesome")
    } verify { element ->
        element.assertIsEqualTo(null)
    }

    @Test
    fun givenMultipleMatchesGetByTextThrows() = givenMultipleElementsByTextErrors(::getByText)

    @Test
    fun givenMultipleMatchesFindByTextThrows() = givenMultipleElementsByTextErrors(::findByText)

    @Test
    fun givenMultipleMatchesQueryByTextThrows() = givenMultipleElementsByTextErrors(::queryByText)

    private fun givenMultipleElementsByTextErrors(query: suspend (text: String) -> HTMLElement?) = testingLibrarySetup {
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
    fun givenMultipleMatchesGetAllByTextReturnsAllMatches() = givenMultipleElementsByTextSucceeds(::getAllByText)

    @Test
    fun givenMultipleMatchesFindAllByTextReturnsAllMatches() = givenMultipleElementsByTextSucceeds(::findAllByText)

    @Test
    fun givenMultipleMatchesQueryAllByTextReturnsAllMatches() = givenMultipleElementsByTextSucceeds(::queryAllByText)

    private fun givenMultipleElementsByTextSucceeds(query: suspend (text: String) -> Array<HTMLElement>) = testingLibrarySetup {
    } exercise {
        query("Cool")
    } verify { elements ->
        elements.map { it.getAttribute("data-test-info") }
            .assertIsEqualTo(listOf("pretty-cool", "very-cool", "extremely-cool"))
        elements.asList().forEach { it.isConnected.assertIsEqualTo(true) }
    }

    @Test
    fun givenOneMatchGetAllByTextReturnsOnlyMatch() = givenSingleElementsByTextSucceeds(::getAllByText)

    @Test
    fun givenOneMatchFindAllByTextReturnsOnlyMatch() = givenSingleElementsByTextSucceeds(::findAllByText)

    @Test
    fun givenOneMatchQueryAllByTextReturnsOnlyMatch() = givenSingleElementsByTextSucceeds(::queryAllByText)

    private fun givenSingleElementsByTextSucceeds(query: suspend (text: String) -> Array<HTMLElement>) = testingLibrarySetup {
    } exercise {
        query("Awesome")
    } verify { elements ->
        elements.map { it.getAttribute("data-test-info") }
            .assertIsEqualTo(listOf("pretty-cool"))
        elements.asList().forEach { it.isConnected.assertIsEqualTo(true) }
    }
}
