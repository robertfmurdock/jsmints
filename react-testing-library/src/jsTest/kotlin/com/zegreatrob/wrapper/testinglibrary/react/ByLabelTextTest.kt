package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.minassert.assertIsEqualTo
import org.w3c.dom.HTMLElement
import kotlin.test.Test

class ByLabelTextTest : ByLabelText by TestingLibraryReact {

    @Test
    fun givenElementExistsCanGetByLabelText() = givenElementByLabelTextWorks(::getByLabelText)

    @Test
    fun givenElementExistsCanFindByLabelText() = givenElementByLabelTextWorks(::findByLabelText)

    @Test
    fun givenElementExistsCanQueryByLabelText() = givenElementByLabelTextWorks(::queryByLabelText)

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
    fun givenNoElementExistsGetByLabelText() = givenNoElementByLabelTextWillFailAsExpected(::getByLabelText)

    @Test
    fun givenNoElementExistsFindByLabelText() = givenNoElementByLabelTextWillFailAsExpected(::findByLabelText)

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
    fun givenNoElementExistsQueryByLabelText() = testingLibrarySetup {
    } exercise {
        queryByLabelText("Not Awesome")
    } verify { element ->
        element.assertIsEqualTo(null)
    }

    @Test
    fun givenMultipleElementExistsErrorsOnGetByLabelText() = givenMultipleElementsByLabelTextErrors(::getByLabelText)

    @Test
    fun givenMultipleElementExistsErrorsOnFindByLabelText() = givenMultipleElementsByLabelTextErrors(::findByLabelText)

    @Test
    fun givenMultipleElementExistsErrorsOnQueryByLabelText() =
        givenMultipleElementsByLabelTextErrors(::queryByLabelText)

    private fun givenMultipleElementsByLabelTextErrors(query: suspend (text: String) -> HTMLElement?) =
        testingLibrarySetup {
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
    fun givenMultipleElementExistsSucceedsOnGetAllByLabelText() =
        givenMultipleElementsByLabelTextSucceeds(::getAllByLabelText)

    @Test
    fun givenMultipleElementExistsSucceedsOnFindAllByLabelText() =
        givenMultipleElementsByLabelTextSucceeds(::findAllByLabelText)

    @Test
    fun givenMultipleElementExistsSucceedsOnQueryAllByLabelText() =
        givenMultipleElementsByLabelTextSucceeds(::queryAllByLabelText)

    private fun givenMultipleElementsByLabelTextSucceeds(query: suspend (text: String) -> Array<HTMLElement>) =
        testingLibrarySetup {
        } exercise {
            query("Chill")
        } verify { elements ->
            elements.map { it.getAttribute("data-test-info") }
                .assertIsEqualTo(listOf("pretty-cool", "very-cool", "extremely-cool"))
            elements.asList().forEach { it.isConnected.assertIsEqualTo(true) }
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

    private fun givenSingleElementsByLabelTextSucceeds(query: suspend (text: String) -> Array<HTMLElement>) =
        testingLibrarySetup {
        } exercise {
            query("Press Me")
        } verify { elements ->
            elements.map { it.getAttribute("data-test-info") }
                .assertIsEqualTo(listOf("pretty-cool"))
            elements.asList().forEach { it.isConnected.assertIsEqualTo(true) }
        }
}
