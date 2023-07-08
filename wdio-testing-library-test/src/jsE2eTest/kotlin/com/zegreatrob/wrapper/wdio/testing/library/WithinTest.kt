package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import kotlin.test.Test

class WithinTest : Within by TestingLibraryBrowser {

    private suspend fun getAwesomeSection() = TestingLibraryBrowser.getByText("Awesome button section").parentElement()
    private suspend fun getCoolSection() = TestingLibraryBrowser.getByText("Cool button section").parentElement()

    @Test
    fun givenElementExistsCanGetByText() = givenElementByTextWorks { within(getAwesomeSection()).getByText(it) }

    @Test
    fun givenElementExistsCanFindByText() = givenElementByTextWorks { within(getAwesomeSection()).findByText(it) }

    @Test
    fun givenElementExistsCanQueryByText() = givenElementByTextWorks { within(getAwesomeSection()).queryByText(it) }

    private fun givenElementByTextWorks(query: suspend (text: String) -> WebdriverElement?) = testingLibrarySetup {
    } exercise {
        query("Awesome")
    } verify { element ->
        element?.isDisplayed()
            .assertIsEqualTo(true)
        element?.attribute("data-test-info")
            .assertIsEqualTo("pretty-cool")
    }

    @Test
    fun givenNoElementExistsGetByText() =
        givenNoElementByTextWillFailAsExpected { within(getCoolSection()).getByText(it) }

    @Test
    fun givenNoElementExistsFindByText() =
        givenNoElementByTextWillFailAsExpected { within(getCoolSection()).findByText(it) }

    private fun givenNoElementByTextWillFailAsExpected(
        query: suspend (text: String) -> WebdriverElement?,
    ) = testingLibrarySetup {
    } exercise {
        kotlin.runCatching { query("Awesome") }
    } verify { result ->
        result.isFailure
            .assertIsEqualTo(true)
        result.exceptionOrNull()?.message.apply {
            this?.startsWith(
                "Unable to find an element with the text: Awesome. " +
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
        within(getCoolSection()).queryByText("Awesome")
    } verify { element ->
        element.isPresent().assertIsEqualTo(false)
        element.isDisplayed().assertIsEqualTo(false)
    }

    @Test
    fun givenMultipleElementExistsErrorsOnGetByText() = givenMultipleElementsByTextErrors {
        within(getCoolSection()).getByText(it)
    }

    @Test
    fun givenMultipleElementExistsErrorsOnFindByText() = givenMultipleElementsByTextErrors {
        within(getCoolSection()).findByText(it)
    }

    @Test
    fun givenMultipleElementExistsErrorsOnQueryByText() = givenMultipleElementsByTextErrors {
        within(getCoolSection()).queryByText(it)
    }

    private fun givenMultipleElementsByTextErrors(query: suspend (text: String) -> WebdriverElement?) =
        testingLibrarySetup {
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
    fun givenMultipleElementExistsSucceedsOnGetAllByText() = givenMultipleElementsByTextSucceeds {
        within(getCoolSection()).getAllByText(it)
    }

    @Test
    fun givenMultipleElementExistsSucceedsOnFindAllByText() = givenMultipleElementsByTextSucceeds {
        within(getCoolSection()).findAllByText(it)
    }

    @Test
    fun givenMultipleElementExistsSucceedsOnQueryAllByText() = givenMultipleElementsByTextSucceeds {
        within(getCoolSection()).queryAllByText(it)
    }

    private fun givenMultipleElementsByTextSucceeds(query: suspend (text: String) -> WebdriverElementArray) =
        testingLibrarySetup {
        } exercise {
            query("Cool")
        } verify { elements ->
            elements.map { it.attribute("data-test-info") }
                .assertIsEqualTo(listOf("pretty-cool", "very-cool", "extremely-cool"))
            elements.asList().forEach { it.isDisplayed().assertIsEqualTo(true) }
        }

    @Test
    fun givenSingleElementExistsSucceedsOnGetAllByText() = givenSingleElementsByTextSucceeds {
        within(getAwesomeSection()).getAllByText(it)
    }

    @Test
    fun givenSingleElementExistsSucceedsOnFindAllByText() = givenSingleElementsByTextSucceeds {
        within(getAwesomeSection()).findAllByText(it)
    }

    @Test
    fun givenSingleElementExistsSucceedsOnQueryAllByText() = givenSingleElementsByTextSucceeds {
        within(getAwesomeSection()).queryAllByText(it)
    }

    private fun givenSingleElementsByTextSucceeds(query: suspend (text: String) -> WebdriverElementArray) =
        testingLibrarySetup {
        } exercise {
            query("Awesome")
        } verify { elements ->
            elements.map { it.attribute("data-test-info") }
                .assertIsEqualTo(listOf("pretty-cool"))
            elements.asList().forEach { it.isDisplayed().assertIsEqualTo(true) }
        }
}
