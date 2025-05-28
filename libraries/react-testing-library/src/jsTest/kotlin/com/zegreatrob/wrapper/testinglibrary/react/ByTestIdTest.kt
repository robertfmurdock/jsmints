package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.minassert.assertIsEqualTo
import org.w3c.dom.HTMLElement
import kotlin.test.Test

class ByTestIdTest : ByTestId by TestingLibraryReact.screen {

    @Test
    fun givenElementExistsCanGetByTestId() = givenElementByTestIdWorks(::getByTestId)

    @Test
    fun givenElementExistsCanFindByTestId() = givenElementByTestIdWorks(::findByTestId)

    @Test
    fun givenElementExistsCanQueryByTestId() = givenElementByTestIdWorks(::queryByTestId)

    private fun givenElementByTestIdWorks(query: suspend (testId: String) -> HTMLElement?) = testingLibrarySetup {
    } exercise {
        query("Awesome-testId")
    } verify { element ->
        element?.isConnected
            .assertIsEqualTo(true)
        element?.getAttribute("data-testid")
            .assertIsEqualTo("Awesome-testId")
    }

    @Test
    fun givenNoElementExistsGetByTestId() = givenNoElementByTestIdWillFailAsExpected(::getByTestId)

    @Test
    fun givenNoElementExistsFindByTestId() = givenNoElementByTestIdWillFailAsExpected(::findByTestId)

    private fun givenNoElementByTestIdWillFailAsExpected(
        query: suspend (testId: String) -> HTMLElement?,
    ) = testingLibrarySetup {
    } exercise {
        runCatching { query("NotAwesome-testId") }
    } verify { result ->
        result.isFailure
            .assertIsEqualTo(true)
        result.exceptionOrNull()?.message.apply {
            this?.startsWith(
                "Unable to find an element by: [data-testid=\"NotAwesome-testId\"]",
            )
                .assertIsEqualTo(true, this)
        }
    }

    @Test
    fun givenNoElementExistsQueryByTestId() = testingLibrarySetup {
    } exercise {
        queryByTestId("NotAwesome-testId")
    } verify { element ->
        element.assertIsEqualTo(null)
    }

    @Test
    fun givenMultipleElementExistsErrorsOnGetByTestId() = givenMultipleElementsByTestIdErrors(::getByTestId)

    @Test
    fun givenMultipleElementExistsErrorsOnFindByTestId() = givenMultipleElementsByTestIdErrors(::findByTestId)

    @Test
    fun givenMultipleElementExistsErrorsOnQueryByTestId() = givenMultipleElementsByTestIdErrors(::queryByTestId)

    private fun givenMultipleElementsByTestIdErrors(query: suspend (testId: String) -> HTMLElement?) = testingLibrarySetup {
    } exercise {
        runCatching { query("Cool-testId") }
    } verify { result ->
        result.isFailure
            .assertIsEqualTo(true)
        result.exceptionOrNull()?.message.apply {
            this?.startsWith("Found multiple elements by: [data-testid=\"Cool-testId\"]")
                .assertIsEqualTo(true, "<$this>")
        }
    }

    @Test
    fun givenMultipleElementExistsSucceedsOnGetAllByTestId() = givenMultipleElementsByTestIdSucceeds(::getAllByTestId)

    @Test
    fun givenMultipleElementExistsSucceedsOnFindAllByTestId() = givenMultipleElementsByTestIdSucceeds(::findAllByTestId)

    @Test
    fun givenMultipleElementExistsSucceedsOnQueryAllByTestId() = givenMultipleElementsByTestIdSucceeds(::queryAllByTestId)

    private fun givenMultipleElementsByTestIdSucceeds(query: suspend (testId: String) -> Array<HTMLElement>) = testingLibrarySetup {
    } exercise {
        query("Cool-testId")
    } verify { elements ->
        elements.map { it.getAttribute("data-test-info") }
            .assertIsEqualTo(listOf("pretty-cool", "very-cool", "extremely-cool"))
        elements.asList().forEach { it.isConnected.assertIsEqualTo(true) }
    }

    @Test
    fun givenSingleElementExistsSucceedsOnGetAllByTestId() = givenSingleElementsByTestIdSucceeds(::getAllByTestId)

    @Test
    fun givenSingleElementExistsSucceedsOnFindAllByTestId() = givenSingleElementsByTestIdSucceeds(::findAllByTestId)

    @Test
    fun givenSingleElementExistsSucceedsOnQueryAllByTestId() = givenSingleElementsByTestIdSucceeds(::queryAllByTestId)

    private fun givenSingleElementsByTestIdSucceeds(query: suspend (testId: String) -> Array<HTMLElement>) = testingLibrarySetup {
    } exercise {
        query("Awesome-testId")
    } verify { elements ->
        elements.map { it.getAttribute("data-test-info") }
            .assertIsEqualTo(listOf("pretty-cool"))
        elements.asList().forEach { it.isConnected.assertIsEqualTo(true) }
    }
}
