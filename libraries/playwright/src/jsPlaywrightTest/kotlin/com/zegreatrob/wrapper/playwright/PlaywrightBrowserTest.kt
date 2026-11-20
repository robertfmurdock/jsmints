package com.zegreatrob.wrapper.playwright

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlaywrightBrowserTest {
    @Test
    fun roleLocatorClicksInsideItsSection() = documentSetup(
        """
            <section id="inside"><button onclick="this.textContent = 'Saved'">Save</button></section>
            <section><button>Save</button></section>
        """.trimIndent(),
    ) exercise {
        locator("#inside").getByRole("button", name = "Save", exact = true).click()
    } verify {
        val section = locator("#inside")
        assertEquals(1, section.getByRole("button", name = "Saved", exact = true).count())
    }

    @Test
    fun evaluateAwaitsAPagePromiseWithAnExplicitArgument() = playwrightSetup { page ->
        object {
            val page = page
            val text = "value from Node"
            val pageFunction: (String) -> Promise<String> =
                js("(value) => Promise.resolve(document.location.protocol + value)")
        }
    } exercise {
        page.evaluate(pageFunction, text)
    } verify { result ->
        assertEquals("about:$text", result)
    }

    @Test
    fun exactTextWithoutAMatchHasAnEmptyCollection() = documentSetup(
        "<p>Save changes</p>",
    ) exercise {
        getByText("Save", exact = true).all()
    } verify { matches ->
        assertEquals(emptyList(), matches)
    }

    @Test
    fun labelLocatorReturnsItsControlAndPreservesANullAttribute() = documentSetup(
        """<label>Name <input id="name"></label>""",
    ) exercise {
        val control = getByLabel("Name", exact = true)
        control.getAttribute("id") to control.getAttribute("placeholder")
    } verify { (id, placeholder) ->
        assertEquals("name", id)
        assertNull(placeholder)
    }

    @Test
    fun scopedLabelMatchesKeepDocumentOrder() = documentSetup(
        """
            <section id="inside">
                <label>Name <input id="first"></label>
                <label>Name <input id="second"></label>
            </section>
            <label>Name <input id="outside"></label>
        """.trimIndent(),
    ) exercise {
        locator("#inside").getByLabel("Name").all().map { it.getAttribute("id") }
    } verify { ids ->
        assertEquals(listOf("first", "second"), ids)
    }

    @Test
    fun duplicateTextFailsWhenResolvingASingleAttribute() = documentSetup(
        "<p>Save</p><p>Save</p>",
    ) exercise {
        runCatching { getByText("Save").getAttribute("id") }.exceptionOrNull()?.message.orEmpty()
    } verify { message ->
        assertContains(message, "strict mode violation")
        assertContains(message, "2 elements")
    }

    @Test
    fun scopedTextLocatorWaitsForAnInitiallyHiddenMatch() = documentSetup(
        """
            <section id="inside"><p hidden>Saved</p></section>
            <button onclick="document.querySelector('p').hidden = false">Show</button>
        """.trimIndent(),
    ) exercise {
        val status = locator("#inside").getByText("Saved")
        coroutineScope {
            val waiting = async(start = CoroutineStart.UNDISPATCHED) {
                status.waitForVisible(timeoutMillis = 1500)
            }
            val visibleBeforeReveal = status.isVisible()
            val pendingBeforeReveal = waiting.isActive
            getByRole("button", name = "Show").click()
            waiting.await()
            Triple(visibleBeforeReveal, pendingBeforeReveal, status.isVisible())
        }
    } verify { (visibleBeforeReveal, pendingBeforeReveal, visibleAfterReveal) ->
        assertFalse(visibleBeforeReveal)
        assertTrue(pendingBeforeReveal, "Visibility wait should still be pending while the match is hidden")
        assertTrue(visibleAfterReveal)
    }

    @Test
    fun findingOneWaitsForTheDuplicateToDisappear() = documentSetup(
        """
            <p id="retained">Saved</p><p id="duplicate">Saved</p>
            <button onclick="document.querySelector('#duplicate').remove()">Remove duplicate</button>
        """.trimIndent(),
    ) exercise {
        val status = getByText("Saved", exact = true)
        coroutineScope {
            val match = async(start = CoroutineStart.UNDISPATCHED) {
                status.findOne("saved status", timeoutMillis = 1500)
            }
            val countBeforeRemoval = status.count()
            val pendingBeforeRemoval = match.isActive
            getByRole("button", name = "Remove duplicate").click()
            Triple(countBeforeRemoval, pendingBeforeRemoval, match.await().getAttribute("id"))
        }
    } verify { (countBeforeRemoval, pendingBeforeRemoval, id) ->
        assertEquals(2, countBeforeRemoval)
        assertTrue(pendingBeforeRemoval, "Single-match lookup should still be pending while both matches exist")
        assertEquals("retained", id)
    }

    @Test
    fun findingAllWaitsForTheCollectionToAppear() = documentSetup(
        """
            <button onclick="this.insertAdjacentHTML('afterend', '<p id=first>Saved</p><p id=second>Saved</p>')">
                Insert matches
            </button>
        """.trimIndent(),
    ) exercise {
        val matches = getByText("Saved", exact = true)
        coroutineScope {
            val results = async(start = CoroutineStart.UNDISPATCHED) {
                matches.findAll(timeoutMillis = 1500)
            }
            val countBeforeInsertion = matches.count()
            val pendingBeforeInsertion = results.isActive
            getByRole("button", name = "Insert matches").click()
            Triple(countBeforeInsertion, pendingBeforeInsertion, results.await().map { it.getAttribute("id") })
        }
    } verify { (countBeforeInsertion, pendingBeforeInsertion, ids) ->
        assertEquals(0, countBeforeInsertion)
        assertTrue(pendingBeforeInsertion, "Collection lookup should still be pending while no matches exist")
        assertEquals(listOf("first", "second"), ids)
    }
}
