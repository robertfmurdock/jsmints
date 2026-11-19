package com.zegreatrob.wrapper.playwright

import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlaywrightBrowserTest {
    @Test
    fun roleLocatorClicksInsideItsSection() = asyncSetup(object {
        val html = """
            <section id="inside"><button onclick="this.textContent = 'Saved'">Save</button></section>
            <section><button>Save</button></section>
        """.trimIndent()
    }) exercise {
        withPage { page ->
            page.goto("data:text/html,${encodeURIComponent(html)}")
            val section = page.locator("#inside")
            section.getByRole("button", name = "Save", exact = true).click()
            section.getByRole("button", name = "Saved", exact = true).count()
        }
    } verify { count ->
        assertEquals(1, count)
    }

    @Test
    fun evaluateAwaitsAPagePromiseWithAnExplicitArgument() = asyncSetup(object {
        val text = "value from Node"
        val pageFunction: (String) -> Promise<String> =
            js("(value) => Promise.resolve(document.location.protocol + value)")
    }) exercise {
        withPage { page ->
            page.evaluate(pageFunction, text)
        }
    } verify { result ->
        assertEquals("about:$text", result)
    }

    @Test
    fun exactTextWithoutAMatchHasAnEmptyCollection() = asyncSetup(object {
        val html = "<p>Save changes</p>"
    }) exercise {
        withPage { page ->
            page.goto("data:text/html,${encodeURIComponent(html)}")
            page.getByText("Save", exact = true).all()
        }
    } verify { matches ->
        assertEquals(emptyList(), matches)
    }

    @Test
    fun labelLocatorReturnsItsControlAndPreservesANullAttribute() = asyncSetup(object {
        val html = """<label>Name <input id="name"></label>"""
    }) exercise {
        withPage { page ->
            page.goto("data:text/html,${encodeURIComponent(html)}")
            val control = page.getByLabel("Name", exact = true)
            control.getAttribute("id") to control.getAttribute("placeholder")
        }
    } verify { (id, placeholder) ->
        assertEquals("name", id)
        assertNull(placeholder)
    }

    @Test
    fun scopedLabelMatchesKeepDocumentOrder() = asyncSetup(object {
        val html = """
            <section id="inside">
                <label>Name <input id="first"></label>
                <label>Name <input id="second"></label>
            </section>
            <label>Name <input id="outside"></label>
        """.trimIndent()
    }) exercise {
        withPage { page ->
            page.goto("data:text/html,${encodeURIComponent(html)}")
            page.locator("#inside").getByLabel("Name").all().map { it.getAttribute("id") }
        }
    } verify { ids ->
        assertEquals(listOf("first", "second"), ids)
    }

    @Test
    fun duplicateTextFailsWhenResolvingASingleAttribute() = asyncSetup(object {
        val html = "<p>Save</p><p>Save</p>"
    }) exercise {
        withPage { page ->
            page.goto("data:text/html,${encodeURIComponent(html)}")
            runCatching { page.getByText("Save").getAttribute("id") }.exceptionOrNull()?.message.orEmpty()
        }
    } verify { message ->
        assertContains(message, "strict mode violation")
        assertContains(message, "2 elements")
    }

    @Test
    fun scopedTextLocatorWaitsForAnInitiallyHiddenMatch() = asyncSetup(object {
        val html = """
            <section id="inside"><p hidden>Saved</p></section>
            <button onclick="document.querySelector('p').hidden = false">Show</button>
        """.trimIndent()
    }) exercise {
        withPage { page ->
            page.goto("data:text/html,${encodeURIComponent(html)}")
            val status = page.locator("#inside").getByText("Saved")
            val initiallyVisible = status.isVisible()
            coroutineScope {
                val waiting = async(start = CoroutineStart.UNDISPATCHED) {
                    status.waitForVisible(timeoutMillis = 1500)
                    status.isVisible()
                }
                page.getByRole("button", name = "Show").click()
                initiallyVisible to waiting.await()
            }
        }
    } verify { observation ->
        assertEquals(false to true, observation)
    }

    private suspend fun <T> withPage(test: suspend (Page) -> T): T {
        val browser = Playwright.chromium.launch()
        try {
            val context = browser.newContext()
            try {
                return test(context.newPage())
            } finally {
                context.close()
            }
        } finally {
            browser.close()
        }
    }
}

private external fun encodeURIComponent(value: String): String
