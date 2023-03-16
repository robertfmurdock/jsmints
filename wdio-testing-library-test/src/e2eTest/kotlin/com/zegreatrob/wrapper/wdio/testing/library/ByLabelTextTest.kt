package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import kotlin.test.Test

class ByLabelTextTest : ByLabelText by TestingLibraryBrowser {

    @Test
    fun givenElementExistsCanGetByLabelText() = givenElementByLabelTextWorks(::getByLabelText)

    @Test
    fun givenElementExistsCanFindByLabelText() = givenElementByLabelTextWorks(::findByLabelText)

    @Test
    fun givenElementExistsCanQueryByLabelText() = givenElementByLabelTextWorks(::queryByLabelText)

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
    fun givenNoElementExistsGetByLabelText() = givenNoElementByLabelTextWillFailAsExpected(::getByLabelText)

    @Test
    fun givenNoElementExistsFindByLabelText() = givenNoElementByLabelTextWillFailAsExpected(::findByLabelText)

    private fun givenNoElementByLabelTextWillFailAsExpected(
        query: suspend (text: String) -> WebdriverElement?,
    ) = testingLibrarySetup {
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
    fun givenNoElementExistsQueryByLabelText() = testingLibrarySetup {
    } exercise {
        queryByLabelText("Not Awesome")
    } verify { element ->
        element.isPresent().assertIsEqualTo(false)
        element.isDisplayed().assertIsEqualTo(false)
    }

    @Test
    fun givenMultipleElementExistsErrorsOnGetByLabelText() = givenMultipleElementsByLabelTextErrors(::getByLabelText)

    @Test
    fun givenMultipleElementExistsErrorsOnFindByLabelText() = givenMultipleElementsByLabelTextErrors(::findByLabelText)

    @Test
    fun givenMultipleElementExistsErrorsOnQueryByLabelText() =
        givenMultipleElementsByLabelTextErrors(::queryByLabelText)

    private fun givenMultipleElementsByLabelTextErrors(query: suspend (text: String) -> WebdriverElement?) =
        testingLibrarySetup {
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
    fun givenMultipleElementExistsSucceedsOnGetAllByLabelText() =
        givenMultipleElementsByLabelTextSucceeds(::getAllByLabelText)

    @Test
    fun givenMultipleElementExistsSucceedsOnFindAllByLabelText() =
        givenMultipleElementsByLabelTextSucceeds(::findAllByLabelText)

    @Test
    fun givenMultipleElementExistsSucceedsOnQueryAllByLabelText() =
        givenMultipleElementsByLabelTextSucceeds(::queryAllByLabelText)

    private fun givenMultipleElementsByLabelTextSucceeds(query: suspend (text: String) -> WebdriverElementArray) =
        testingLibrarySetup {
        } exercise {
            query("Chill")
        } verify { elements ->
            elements.map { it.attribute("data-test-info") }
                .assertIsEqualTo(listOf("pretty-cool", "very-cool", "extremely-cool"))
            elements.asList().forEach { it.isDisplayed().assertIsEqualTo(true) }
        }

    @Test
    fun givenSingleElementExistsSucceedsOnGetAllByLabelText() =
        givenSingleElementsByLabelTextSucceeds(::getAllByLabelText)

    @Test
    fun givenSingleElementExistsSucceedsOnFindAllByLabelText() =
        givenSingleElementsByLabelTextSucceeds(::findAllByLabelText)

    @Test
    fun givenSingleElementExistsSucceedsOnQueryAllByLabelText() =
        givenSingleElementsByLabelTextSucceeds(::queryAllByLabelText)

    private fun givenSingleElementsByLabelTextSucceeds(query: suspend (text: String) -> WebdriverElementArray) =
        testingLibrarySetup {
        } exercise {
            query("Press Me")
        } verify { elements ->
            elements.map { it.attribute("data-test-info") }
                .assertIsEqualTo(listOf("pretty-cool"))
            elements.asList().forEach { it.isDisplayed().assertIsEqualTo(true) }
        }
}
