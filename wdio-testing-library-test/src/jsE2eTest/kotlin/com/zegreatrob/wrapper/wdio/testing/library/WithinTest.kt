package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import kotlin.test.Test

class WithinTest : Within by TestingLibraryBrowser {

    private suspend fun getAwesomeSection() = TestingLibraryBrowser.getByText("Awesome button section").parentElement()
    private suspend fun getCoolSection() = TestingLibraryBrowser.getByText("Cool button section").parentElement()

    @Test
    fun givenOneMatchGetByTextReturnsMatch() = givenElementByTextWorks { within(getAwesomeSection()).getByText(it) }

    @Test
    fun givenOneMatchFindByTextReturnsMatch() = givenElementByTextWorks { within(getAwesomeSection()).findByText(it) }

    @Test
    fun givenOneMatchQueryByTextReturnsMatch() = givenElementByTextWorks { within(getAwesomeSection()).queryByText(it) }

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
    fun givenNoMatchGetByTextThrows() = givenNoElementByTextWillFailAsExpected { within(getCoolSection()).getByText(it) }

    @Test
    fun givenNoMatchFindByTextThrows() = givenNoElementByTextWillFailAsExpected { within(getCoolSection()).findByText(it) }

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
    fun givenNoMatchQueryByTextReportsNoElement() = testingLibrarySetup {
    } exercise {
        within(getCoolSection()).queryByText("Awesome")
    } verify { element ->
        element.isPresent().assertIsEqualTo(false)
        element.isDisplayed().assertIsEqualTo(false)
    }

    @Test
    fun givenMultipleMatchesGetByTextThrows() = givenMultipleElementsByTextErrors {
        within(getCoolSection()).getByText(it)
    }

    @Test
    fun givenMultipleMatchesFindByTextThrows() = givenMultipleElementsByTextErrors {
        within(getCoolSection()).findByText(it)
    }

    @Test
    fun givenMultipleMatchesQueryByTextThrows() = givenMultipleElementsByTextErrors {
        within(getCoolSection()).queryByText(it)
    }

    private fun givenMultipleElementsByTextErrors(query: suspend (text: String) -> WebdriverElement?) = testingLibrarySetup {
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
    fun givenMultipleMatchesGetAllByTextReturnsAllMatches() = givenMultipleElementsByTextSucceeds {
        within(getCoolSection()).getAllByText(it)
    }

    @Test
    fun givenMultipleMatchesFindAllByTextReturnsAllMatches() = givenMultipleElementsByTextSucceeds {
        within(getCoolSection()).findAllByText(it)
    }

    @Test
    fun givenMultipleMatchesQueryAllByTextReturnsAllMatches() = givenMultipleElementsByTextSucceeds {
        within(getCoolSection()).queryAllByText(it)
    }

    private fun givenMultipleElementsByTextSucceeds(query: suspend (text: String) -> WebdriverElementArray) = testingLibrarySetup {
    } exercise {
        query("Cool")
    } verify { elements ->
        elements.map { it.attribute("data-test-info") }
            .assertIsEqualTo(listOf("pretty-cool", "very-cool", "extremely-cool"))
        elements.asList().forEach { it.isDisplayed().assertIsEqualTo(true) }
    }

    @Test
    fun givenOneMatchGetAllByTextReturnsOnlyMatch() = givenSingleElementsByTextSucceeds {
        within(getAwesomeSection()).getAllByText(it)
    }

    @Test
    fun givenOneMatchFindAllByTextReturnsOnlyMatch() = givenSingleElementsByTextSucceeds {
        within(getAwesomeSection()).findAllByText(it)
    }

    @Test
    fun givenOneMatchQueryAllByTextReturnsOnlyMatch() = givenSingleElementsByTextSucceeds {
        within(getAwesomeSection()).queryAllByText(it)
    }

    private fun givenSingleElementsByTextSucceeds(query: suspend (text: String) -> WebdriverElementArray) = testingLibrarySetup {
    } exercise {
        query("Awesome")
    } verify { elements ->
        elements.map { it.attribute("data-test-info") }
            .assertIsEqualTo(listOf("pretty-cool"))
        elements.asList().forEach { it.isDisplayed().assertIsEqualTo(true) }
    }
}
