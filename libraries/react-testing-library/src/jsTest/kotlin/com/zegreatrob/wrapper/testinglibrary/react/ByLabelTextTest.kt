package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.minassert.assertIsEqualTo
import org.w3c.dom.HTMLElement
import kotlin.test.Test

class ByLabelTextTest : ByLabelText by TestingLibraryReact.screen {

    @Test
    fun givenOneMatchGetByLabelTextReturnsMatch() = givenElementByLabelTextWorks(::getByLabelText)

    @Test
    fun givenOneMatchFindByLabelTextReturnsMatch() = givenElementByLabelTextWorks(::findByLabelText)

    @Test
    fun givenOneMatchQueryByLabelTextReturnsMatch() = givenElementByLabelTextWorks(::queryByLabelText)

    private fun givenElementByLabelTextWorks(query: suspend (text: String) -> HTMLElement?) = testingLibrarySetup {
    } exercise {
        query("Press Me")
    } verify { element ->
        element?.isConnected
            .assertIsEqualTo(true)
        element?.getAttribute("data-test-info")
            .assertIsEqualTo("pretty-cool")
    }

    @Test
    fun givenNoMatchGetByLabelTextThrows() = givenNoElementByLabelTextWillFailAsExpected(::getByLabelText)

    @Test
    fun givenNoMatchFindByLabelTextThrows() = givenNoElementByLabelTextWillFailAsExpected(::findByLabelText)

    private fun givenNoElementByLabelTextWillFailAsExpected(
        query: suspend (text: String) -> HTMLElement?,
    ) = testingLibrarySetup {
    } exercise {
        runCatching { query("Not Awesome") }
    } verify { result ->
        result.isFailure
            .assertIsEqualTo(true)
        result.exceptionOrNull()?.message.apply {
            this?.startsWith(
                "Unable to find a label with the text of: Not Awesome",
            )
                .assertIsEqualTo(true, this)
        }
    }

    @Test
    fun givenNoMatchQueryByLabelTextReturnsNull() = testingLibrarySetup {
    } exercise {
        queryByLabelText("Not Awesome")
    } verify { element ->
        element.assertIsEqualTo(null)
    }

    @Test
    fun givenMultipleMatchesGetByLabelTextThrows() = givenMultipleElementsByLabelTextErrors(::getByLabelText)

    @Test
    fun givenMultipleMatchesFindByLabelTextThrows() = givenMultipleElementsByLabelTextErrors(::findByLabelText)

    @Test
    fun givenMultipleMatchesQueryByLabelTextThrows() = givenMultipleElementsByLabelTextErrors(::queryByLabelText)

    private fun givenMultipleElementsByLabelTextErrors(query: suspend (text: String) -> HTMLElement?) = testingLibrarySetup {
    } exercise {
        runCatching { query("Chill") }
    } verify { result ->
        result.isFailure
            .assertIsEqualTo(true)
        result.exceptionOrNull()?.message.apply {
            this?.startsWith("Found multiple elements with the text of: Chill")
                .assertIsEqualTo(true, "<$this>")
        }
    }

    @Test
    fun givenMultipleMatchesGetAllByLabelTextReturnsAllMatches() = givenMultipleElementsByLabelTextSucceeds(::getAllByLabelText)

    @Test
    fun givenMultipleMatchesFindAllByLabelTextReturnsAllMatches() = givenMultipleElementsByLabelTextSucceeds(::findAllByLabelText)

    @Test
    fun givenMultipleMatchesQueryAllByLabelTextReturnsAllMatches() = givenMultipleElementsByLabelTextSucceeds(::queryAllByLabelText)

    private fun givenMultipleElementsByLabelTextSucceeds(query: suspend (text: String) -> Array<HTMLElement>) = testingLibrarySetup {
    } exercise {
        query("Chill")
    } verify { elements ->
        elements.map { it.getAttribute("data-test-info") }
            .assertIsEqualTo(listOf("pretty-cool", "very-cool", "extremely-cool"))
        elements.asList().forEach { it.isConnected.assertIsEqualTo(true) }
    }

    @Test
    fun givenOneMatchGetAllByLabelTextReturnsOnlyMatch() = givenSingleElementsByLabelTextSucceeds(::getAllByLabelText)

    @Test
    fun givenOneMatchFindAllByLabelTextReturnsOnlyMatch() = givenSingleElementsByLabelTextSucceeds(::findAllByLabelText)

    @Test
    fun givenOneMatchQueryAllByLabelTextReturnsOnlyMatch() = givenSingleElementsByLabelTextSucceeds(::queryAllByLabelText)

    private fun givenSingleElementsByLabelTextSucceeds(query: suspend (text: String) -> Array<HTMLElement>) = testingLibrarySetup {
    } exercise {
        query("Press Me")
    } verify { elements ->
        elements.map { it.getAttribute("data-test-info") }
            .assertIsEqualTo(listOf("pretty-cool"))
        elements.asList().forEach { it.isConnected.assertIsEqualTo(true) }
    }
}
