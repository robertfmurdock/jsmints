package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import kotlin.test.Test

class TestingLibraryByTextTest {

    @Test
    fun givenElementExistsCanGetByText() = givenElementByTextWorks(TestingLibraryBrowser::getByText)

    @Test
    fun givenElementExistsCanFindByText() = givenElementByTextWorks(TestingLibraryBrowser::findByText)

    @Test
    fun givenElementExistsCanQueryByText() = givenElementByTextWorks(TestingLibraryBrowser::queryByText)

    private fun givenElementByTextWorks(query: suspend (text: String) -> WebdriverElement?) = asyncSetup {
        WebdriverBrowser.setUrl("https://static.localhost")
    } exercise {
        query("Awesome")
    } verify { element ->
        element?.isDisplayed()
            .assertIsEqualTo(true)
        element?.attribute("data-test-info")
            .assertIsEqualTo("pretty-cool")
    }

    @Test
    fun givenNoElementExistsGetByText() = givenNoElementByTextWillFailAsExpected(TestingLibraryBrowser::getByText)

    @Test
    fun givenNoElementExistsFindByText() = givenNoElementByTextWillFailAsExpected(TestingLibraryBrowser::findByText)

    private fun givenNoElementByTextWillFailAsExpected(
        query: suspend (text: String) -> WebdriverElement?
    ) = asyncSetup {
        WebdriverBrowser.setUrl("https://static.localhost")
    } exercise {
        kotlin.runCatching { query("Not Awesome") }
    } verify { result ->
        result.isFailure
            .assertIsEqualTo(true)
        result.exceptionOrNull()?.message.apply {
            this?.startsWith(
                "Unable to find an element with the text: Not Awesome. " +
                    "This could be because the text is broken up by multiple elements. " +
                    "In this case, you can provide a function for your text matcher to " +
                    "make your matcher more flexible."
            )
                .assertIsEqualTo(true, this)
        }
    }

    @Test
    fun givenNoElementExistsQueryByText() = asyncSetup(object {
        val browser = TestingLibraryBrowser
    }) {
        WebdriverBrowser.setUrl("https://static.localhost")
    } exercise {
        browser.queryByText("Not Awesome")
    } verify { element ->
        element.isPresent().assertIsEqualTo(false)
        element.isDisplayed().assertIsEqualTo(false)
    }

    @Test
    fun givenMultipleElementExistsErrorsOnGetByText() =
        givenMultipleElementsByTextErrors(TestingLibraryBrowser::getByText)

    @Test
    fun givenMultipleElementExistsErrorsOnFindByText() =
        givenMultipleElementsByTextErrors(TestingLibraryBrowser::findByText)

    @Test
    fun givenMultipleElementExistsErrorsOnQueryByText() =
        givenMultipleElementsByTextErrors(TestingLibraryBrowser::queryByText)

    private fun givenMultipleElementsByTextErrors(query: suspend (text: String) -> WebdriverElement?) = asyncSetup {
        WebdriverBrowser.setUrl("https://static.localhost")
    } exercise {
        kotlin.runCatching { query("Cool")?.waitToExist() }
    } verify { result ->
        result.isFailure
            .assertIsEqualTo(true)
        result.exceptionOrNull()?.message.apply {
            this?.startsWith("Found multiple elements with the text: Cool")
                .assertIsEqualTo(true, "<$this>")
        }
    }

    @Test
    fun givenMultipleElementExistsSucceedsOnGetAllByText() =
        givenMultipleElementsByTextSucceeds(TestingLibraryBrowser::getAllByText)

    @Test
    fun givenMultipleElementExistsSucceedsOnFindAllByText() =
        givenMultipleElementsByTextSucceeds(TestingLibraryBrowser::findAllByText)

    @Test
    fun givenMultipleElementExistsSucceedsOnQueryAllByText() =
        givenMultipleElementsByTextSucceeds(TestingLibraryBrowser::queryAllByText)

    private fun givenMultipleElementsByTextSucceeds(query: suspend (text: String) -> WebdriverElementArray) =
        asyncSetup {
            WebdriverBrowser.setUrl("https://static.localhost")
        } exercise {
            query("Cool")
        } verify { elements ->
            elements.map { it.attribute("data-test-info") }
                .assertIsEqualTo(listOf("pretty-cool", "very-cool", "extremely-cool"))
            elements.asList().forEach { it.isDisplayed().assertIsEqualTo(true) }
        }

    @Test
    fun givenSingleElementExistsSucceedsOnGetAllByText() =
        givenSingleElementsByTextSucceeds(TestingLibraryBrowser::getAllByText)

    @Test
    fun givenSingleElementExistsSucceedsOnFindAllByText() =
        givenSingleElementsByTextSucceeds(TestingLibraryBrowser::findAllByText)

    @Test
    fun givenSingleElementExistsSucceedsOnQueryAllByText() =
        givenSingleElementsByTextSucceeds(TestingLibraryBrowser::queryAllByText)

    private fun givenSingleElementsByTextSucceeds(query: suspend (text: String) -> WebdriverElementArray) = asyncSetup {
        WebdriverBrowser.setUrl("https://static.localhost")
    } exercise {
        query("Awesome")
    } verify { elements ->
        elements.map { it.attribute("data-test-info") }
            .assertIsEqualTo(listOf("pretty-cool"))
        elements.asList().forEach { it.isDisplayed().assertIsEqualTo(true) }
    }
}
