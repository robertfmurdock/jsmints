package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import kotlin.test.Test

class ByLabelTextTest {

    @Test
    fun givenElementExistsCanGetByLabelText() = givenElementByLabelTextWorks(TestingLibraryBrowser::getByLabelText)

    @Test
    fun givenElementExistsCanFindByLabelText() = givenElementByLabelTextWorks(TestingLibraryBrowser::findByLabelText)

    @Test
    fun givenElementExistsCanQueryByLabelText() = givenElementByLabelTextWorks(TestingLibraryBrowser::queryByLabelText)

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
    fun givenNoElementExistsGetByLabelText() = givenNoElementByLabelTextWillFailAsExpected(TestingLibraryBrowser::getByLabelText)

    @Test
    fun givenNoElementExistsFindByLabelText() = givenNoElementByLabelTextWillFailAsExpected(TestingLibraryBrowser::findByLabelText)

    private fun givenNoElementByLabelTextWillFailAsExpected(
        query: suspend (text: String) -> WebdriverElement?
    ) = testingLibrarySetup {
    } exercise {
        kotlin.runCatching { query("Not Awesome") }
    } verify { result ->
        result.isFailure
            .assertIsEqualTo(true)
        result.exceptionOrNull()?.message.apply {
            this?.startsWith(
                "Unable to find a label with the text of: Not Awesome"
            )
                .assertIsEqualTo(true, this)
        }
    }

    @Test
    fun givenNoElementExistsQueryByLabelText() = testingLibrarySetup(object {
        val browser = TestingLibraryBrowser
    }) exercise {
        browser.queryByLabelText("Not Awesome")
    } verify { element ->
        element.isPresent().assertIsEqualTo(false)
        element.isDisplayed().assertIsEqualTo(false)
    }

    @Test
    fun givenMultipleElementExistsErrorsOnGetByLabelText() =
        givenMultipleElementsByLabelTextErrors(TestingLibraryBrowser::getByLabelText)

    @Test
    fun givenMultipleElementExistsErrorsOnFindByLabelText() =
        givenMultipleElementsByLabelTextErrors(TestingLibraryBrowser::findByLabelText)

    @Test
    fun givenMultipleElementExistsErrorsOnQueryByLabelText() =
        givenMultipleElementsByLabelTextErrors(TestingLibraryBrowser::queryByLabelText)

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
        givenMultipleElementsByLabelTextSucceeds(TestingLibraryBrowser::getAllByLabelText)

    @Test
    fun givenMultipleElementExistsSucceedsOnFindAllByLabelText() =
        givenMultipleElementsByLabelTextSucceeds(TestingLibraryBrowser::findAllByLabelText)

    @Test
    fun givenMultipleElementExistsSucceedsOnQueryAllByLabelText() =
        givenMultipleElementsByLabelTextSucceeds(TestingLibraryBrowser::queryAllByLabelText)

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
        givenSingleElementsByLabelTextSucceeds(TestingLibraryBrowser::getAllByLabelText)

    @Test
    fun givenSingleElementExistsSucceedsOnFindAllByLabelText() =
        givenSingleElementsByLabelTextSucceeds(TestingLibraryBrowser::findAllByLabelText)

    @Test
    fun givenSingleElementExistsSucceedsOnQueryAllByLabelText() =
        givenSingleElementsByLabelTextSucceeds(TestingLibraryBrowser::queryAllByLabelText)

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
