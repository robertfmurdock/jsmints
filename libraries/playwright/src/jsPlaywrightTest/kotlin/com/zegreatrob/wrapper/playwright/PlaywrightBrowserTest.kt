package com.zegreatrob.wrapper.playwright

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
        val initiallyVisible = status.isVisible()
        coroutineScope {
            val waiting = async(start = CoroutineStart.UNDISPATCHED) {
                status.waitForVisible(timeoutMillis = 1500)
                status.isVisible()
            }
            delay(80)
            getByRole("button", name = "Show").click()
            initiallyVisible to waiting.await()
        }
    } verify { observation ->
        assertEquals(false to true, observation)
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
                status.findOne("saved status", timeoutMillis = 1500).getAttribute("id")
            }
            delay(80)
            getByRole("button", name = "Remove duplicate").click()
            match.await()
        }
    } verify { id ->
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
                matches.findAll(timeoutMillis = 1500).map { it.getAttribute("id") }
            }
            delay(80)
            getByRole("button", name = "Insert matches").click()
            results.await()
        }
    } verify { ids ->
        assertEquals(listOf("first", "second"), ids)
    }
}
