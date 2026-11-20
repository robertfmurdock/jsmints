package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import kotlin.test.Test

class ByLabelTextTest : ByLabelText by TestingLibraryBrowser {

    @Test
    fun givenOneMatchGetByLabelTextReturnsMatch() = givenElementByLabelTextWorks(::getByLabelText)

    @Test
    fun givenOneMatchFindByLabelTextReturnsMatch() = givenElementByLabelTextWorks(::findByLabelText)

    @Test
    fun givenOneMatchQueryByLabelTextReturnsMatch() = givenElementByLabelTextWorks(::queryByLabelText)

    private fun givenElementByLabelTextWorks(query: suspend (text: String) -> WebdriverElement?) = testingLibrarySetup {
    } exercise {
        query("Press Me")
    } verify { element ->
        element?.isDisplayed()
            .assertIsEqualTo(true)
        element?.attribute("data-test-info")
            .assertIsEqualTo("pretty-cool")
    }

    @Test
    fun givenNoMatchGetByLabelTextThrows() = givenNoElementByLabelTextWillFailAsExpected(::getByLabelText)

    @Test
    fun givenNoMatchFindByLabelTextThrows() = givenNoElementByLabelTextWillFailAsExpected(::findByLabelText)

    private fun givenNoElementByLabelTextWillFailAsExpected(
        query: suspend (text: String) -> WebdriverElement?,
    ) = staticQueryFailureSetup {
    } exercise {
        kotlin.runCatching { query("Not Awesome") }
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
    fun givenNoMatchQueryByLabelTextReportsNoElement() = testingLibrarySetup {
    } exercise {
        queryByLabelText("Not Awesome")
    } verify { element ->
        element.isPresent().assertIsEqualTo(false)
        element.isDisplayed().assertIsEqualTo(false)
    }

    @Test
    fun givenMultipleMatchesGetByLabelTextThrows() = givenMultipleElementsByLabelTextErrors(::getByLabelText)

    @Test
    fun givenMultipleMatchesFindByLabelTextThrows() = givenMultipleElementsByLabelTextErrors(::findByLabelText)

    @Test
    fun givenMultipleMatchesQueryByLabelTextThrows() = givenMultipleElementsByLabelTextErrors(::queryByLabelText)

    private fun givenMultipleElementsByLabelTextErrors(query: suspend (text: String) -> WebdriverElement?) = staticQueryFailureSetup {
    } exercise {
        kotlin.runCatching { query("Chill")?.waitToExist() }
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

    private fun givenMultipleElementsByLabelTextSucceeds(query: suspend (text: String) -> WebdriverElementArray) = testingLibrarySetup {
    } exercise {
        query("Chill")
    } verify { elements ->
        elements.map { it.attribute("data-test-info") }
            .assertIsEqualTo(listOf("pretty-cool", "very-cool", "extremely-cool"))
        elements.asList().forEach { it.isDisplayed().assertIsEqualTo(true) }
    }

    @Test
    fun givenOneMatchGetAllByLabelTextReturnsOnlyMatch() = givenSingleElementsByLabelTextSucceeds(::getAllByLabelText)

    @Test
    fun givenOneMatchFindAllByLabelTextReturnsOnlyMatch() = givenSingleElementsByLabelTextSucceeds(::findAllByLabelText)

    @Test
    fun givenOneMatchQueryAllByLabelTextReturnsOnlyMatch() = givenSingleElementsByLabelTextSucceeds(::queryAllByLabelText)

    private fun givenSingleElementsByLabelTextSucceeds(query: suspend (text: String) -> WebdriverElementArray) = testingLibrarySetup {
    } exercise {
        query("Press Me")
    } verify { elements ->
        elements.map { it.attribute("data-test-info") }
            .assertIsEqualTo(listOf("pretty-cool"))
        elements.asList().forEach { it.isDisplayed().assertIsEqualTo(true) }
    }
}
