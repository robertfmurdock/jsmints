package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

class ByAltTextTest : ByAltText by TestingLibraryReact.screen {

    @Test
    fun givenOneMatchGetByAltTextReturnsImage() = testingLibrarySetup {
    } exercise {
        getByAltText("A friendly image")
    } verify { image ->
        image.isConnected
            .assertIsEqualTo(true)
        image.tagName
            .assertIsEqualTo("IMG")
        image.getAttribute("alt")
            .assertIsEqualTo("A friendly image")
    }

    @Test
    fun givenOneMatchFindByAltTextReturnsImage() = testingLibrarySetup {
    } exercise {
        findByAltText("A friendly image")
    } verify { image ->
        image.isConnected
            .assertIsEqualTo(true)
        image.tagName
            .assertIsEqualTo("IMG")
        image.getAttribute("alt")
            .assertIsEqualTo("A friendly image")
    }

    @Test
    fun givenOneMatchQueryByAltTextReturnsImage() = testingLibrarySetup {
    } exercise {
        queryByAltText("A friendly image")
    } verify { image ->
        image?.isConnected
            .assertIsEqualTo(true)
        image?.tagName
            .assertIsEqualTo("IMG")
    }

    @Test
    fun givenNoMatchQueryByAltTextReturnsNull() = testingLibrarySetup {
    } exercise {
        queryByAltText("Missing image")
    } verify { image ->
        image.assertIsEqualTo(null)
    }

    @Test
    fun givenNoMatchGetByAltTextThrows() = testingLibrarySetup {
    } exercise {
        runCatching { getByAltText("Missing image") }
    } verify { result ->
        result.exceptionOrNull()?.message?.lineSequence()?.firstOrNull()
            .assertIsEqualTo("Unable to find an element with the alt text: Missing image")
    }

    @Test
    fun givenNoMatchFindByAltTextThrows() = testingLibrarySetup {
    } exercise {
        runCatching { findByAltText("Missing image") }
    } verify { result ->
        result.exceptionOrNull()?.message?.lineSequence()?.firstOrNull()
            .assertIsEqualTo("Unable to find an element with the alt text: Missing image")
    }

    @Test
    fun givenMultipleMatchesGetAllByAltTextReturnsAllMatches() = testingLibrarySetup {
    } exercise {
        getAllByAltText("Shared description")
    } verify { images ->
        images.map { it.getAttribute("data-test-info") }.assertIsEqualTo(listOf("first-image", "second-image"))
    }

    @Test
    fun givenMultipleMatchesFindAllByAltTextReturnsAllMatches() = testingLibrarySetup {
    } exercise {
        findAllByAltText("Shared description")
    } verify { images ->
        images.map { it.getAttribute("data-test-info") }.assertIsEqualTo(listOf("first-image", "second-image"))
    }

    @Test
    fun givenMultipleMatchesQueryAllByAltTextReturnsAllMatches() = testingLibrarySetup {
    } exercise {
        queryAllByAltText("Shared description")
    } verify { images ->
        images.map { it.getAttribute("data-test-info") }.assertIsEqualTo(listOf("first-image", "second-image"))
    }

    @Test
    fun givenNoMatchesQueryAllByAltTextReturnsEmptyArray() = testingLibrarySetup {
    } exercise {
        queryAllByAltText("Missing image")
    } verify { images ->
        images.size.assertIsEqualTo(0)
    }
}
