package com.zegreatrob.wrapper.playwright

import node.buffer.BufferEncoding
import node.buffer.utf8
import node.fs.readFileSync
import node.path.path
import node.process.process
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class LocatorParityTest {
    @Test
    fun requiringOneReturnsTheVisibleMatch() = singletonMatches { requireOne("single") }

    @Test
    fun findingOneReturnsTheVisibleMatch() = singletonMatches { findOne("single", MATCH_TIMEOUT_MILLIS) }

    @Test
    fun queryingOneReturnsTheVisibleMatch() = singletonMatches { queryOne("single") }

    @Test
    fun requiringOneWithoutAMatchReportsAbsence() = queryFailures(ABSENT, "No matches for missing") {
        requireOne("missing")
    }

    @Test
    fun findingOneWithoutAMatchReportsAbsenceAtTheDeadline() = queryFailures(ABSENT, "missing after ${FAILURE_TIMEOUT_MILLIS}ms; found 0") {
        findOne("missing", FAILURE_TIMEOUT_MILLIS)
    }

    @Test
    fun queryingOneWithoutAMatchReturnsNull() = queryFixture(ABSENT, { queryOne("missing") }) verify { result ->
        assertEquals(familyResults(null), result)
    }

    @Test
    fun requiringOneWithDuplicatesReportsAmbiguity() = queryFailures(MULTIPLE, "ambiguous; found 3") {
        requireOne("ambiguous")
    }

    @Test
    fun findingOneWithPersistentDuplicatesReportsAmbiguityAtTheDeadline() = queryFailures(MULTIPLE, "ambiguous after ${FAILURE_TIMEOUT_MILLIS}ms; found 3") {
        findOne("ambiguous", FAILURE_TIMEOUT_MILLIS)
    }

    @Test
    fun queryingOneWithDuplicatesReportsAmbiguity() = queryFailures(MULTIPLE, "ambiguous; found 3") {
        queryOne("ambiguous")
    }

    @Test
    fun requiringAllReturnsTheSingleton() = collectionMatches(SINGLE) { requireAll("collection") }

    @Test
    fun findingAllReturnsTheSingleton() = collectionMatches(SINGLE) { findAll(MATCH_TIMEOUT_MILLIS) }

    @Test
    fun enumeratingReturnsTheSingleton() = collectionMatches(SINGLE) { all() }

    @Test
    fun requiringAllReturnsMultipleMatchesInOrder() = collectionMatches(MULTIPLE) { requireAll("collection") }

    @Test
    fun findingAllReturnsMultipleMatchesInOrder() = collectionMatches(MULTIPLE) { findAll(MATCH_TIMEOUT_MILLIS) }

    @Test
    fun enumeratingReturnsMultipleMatchesInOrder() = collectionMatches(MULTIPLE) { all() }

    private fun singletonMatches(query: suspend Locator.() -> Locator?) = queryFixture(SINGLE, { observe(listOfNotNull(query())) }) verify { results ->
        assertEquals(familyResults(listOf("pretty-cool" to true)), results)
    }

    private fun collectionMatches(index: Int, query: suspend Locator.() -> List<Locator>) = queryFixture(index, { observe(query()) }) verify { results ->
        val expectedIds = if (index == SINGLE) listOf("pretty-cool") else multipleIds
        assertEquals(familyResults(expectedIds.map { it to true }), results)
    }

    private fun queryFailures(index: Int, expected: String, query: suspend Locator.() -> Any?) = queryFixture(index, { runCatching { query() }.exceptionOrNull()?.message.orEmpty() }) verify { results ->
        results.forEach { (family, message) -> assertContains(message, expected, message = family) }
    }

    private fun <T> queryFixture(index: Int, query: suspend Locator.() -> T) = paritySetup exercise {
        mapValues { (_, locators) -> query(locators[index]) }
    }

    private val paritySetup get() = playwrightSetup { page ->
        val folder = requireNotNull(process.env["JSMINTS_PLAYWRIGHT_RESOURCES"])
        page.loadDocument(readFileSync(path.join(folder, "index.html"), BufferEncoding.utf8))
        locatorFamilies(page)
    }

    private suspend fun observe(matches: List<Locator>) = matches.map { it.getAttribute("data-test-info") to it.isVisible() }

    private fun <T> familyResults(value: T) = listOf("role", "text", "label", "scoped text").associateWith { value }

    private fun locatorFamilies(page: Page): Map<String, List<Locator>> {
        val awesome = page.locator("div:has(> h2:text-is('Awesome button section'))")
        val cool = page.locator("div:has(> h2:text-is('Cool button section'))")
        return mapOf(
            "role" to listOf(
                page.getByRole("button", name = "Press Me", exact = true),
                page.getByRole("button", name = "Absent", exact = true),
                page.getByRole("button", name = "Chill", exact = true),
            ),
            "text" to listOf(
                page.getByText("Awesome", exact = true),
                page.getByText("Absent", exact = true),
                page.getByText("Cool", exact = true),
            ),
            "label" to listOf(
                page.getByLabel("Press Me"),
                page.getByLabel("Absent"),
                page.getByLabel("Chill"),
            ),
            "scoped text" to listOf(
                awesome.getByText("Awesome", exact = true),
                cool.getByText("Awesome", exact = true),
                cool.getByText("Cool", exact = true),
            ),
        )
    }

    private companion object {
        const val SINGLE = 0
        const val ABSENT = 1
        const val MULTIPLE = 2
        const val MATCH_TIMEOUT_MILLIS = 1500
        const val FAILURE_TIMEOUT_MILLIS = 60
        val multipleIds = listOf("pretty-cool", "very-cool", "extremely-cool")
    }
}
