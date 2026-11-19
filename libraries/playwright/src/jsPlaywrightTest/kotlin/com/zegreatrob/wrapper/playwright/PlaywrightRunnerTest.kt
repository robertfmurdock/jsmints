package com.zegreatrob.wrapper.playwright

import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlinx.coroutines.await
import node.buffer.BufferEncoding
import node.buffer.utf8
import node.fs.RmOptions
import node.fs.existsSync
import node.fs.mkdtempSync
import node.fs.readFileSync
import node.fs.rmSync
import node.os.tmpdir
import node.path.path
import node.process.process
import kotlin.js.JsModule
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class PlaywrightRunnerTest {
    @Test
    fun loadsPackagedResourcesFromTheNodeRunner() = asyncSetup(object {
        val directory = requireNotNull(process.env["JSMINTS_PLAYWRIGHT_RESOURCES"])
    }) exercise {
        readFileSync(path.join(directory, "cool-file.txt"), BufferEncoding.utf8)
    } verify { contents ->
        assertEquals("Cool File Content", contents)
    }

    @Test
    fun nodeRunnerCanReachAPublicHttpsEndpoint() = asyncSetup(object {
        val endpoint = "https://example.com"
        val fetch: (String) -> Promise<FetchResponse> =
            js("(url) => fetch(url, { signal: AbortSignal.timeout(4000) })")
    }) exercise {
        fetch(endpoint).await().status
    } verify { status ->
        assertEquals(200, status)
    }

    private val traceSetup = asyncTestTemplate(
        sharedSetup = { mkdtempSync(path.join(tmpdir(), "jsmints-playwright-")) },
        sharedTeardown = { directory: String ->
            rmSync(directory, RmOptions(recursive = true, force = true))
        },
    )

    @Test
    fun exerciseFailureRetainsATraceAndClosesThePage() = failingScenario { setup, failure ->
        setup exercise { throw failure } verify {}
    }

    @Test
    fun verificationFailureRetainsATraceAndClosesThePage() = failingScenario { setup, failure ->
        setup exercise { url } verify { throw failure }
    }

    @Test
    fun templateTeardownUsesTheLivePageAndRetainsATraceOnFailure() = traceSetup.with({ directory ->
        object {
            val tracePath = path.join(directory, "teardown.zip")
            val failure = IllegalStateException("teardown failed")
        }
    }) exercise {
        val scenario = playwrightSetupFrom(
            run = { scenario -> playwrightTest(failureTracePath = tracePath, scenario = scenario) },
        ) { page ->
            asyncSetup(page) { goto("about:blank#setup") }
        } exercise {
            goto("about:blank#exercise")
            url
        } verifyAnd { result ->
            assertEquals("about:blank#exercise", result)
        } teardown { result ->
            assertEquals("about:blank#exercise", result)
            goto("about:blank#teardown")
            throw failure
        }
        runCatching { scenario.await() }.exceptionOrNull()
    } verify { actual ->
        assertSame(failure, actual)
        val trace = jsZip.loadAsync(readFileSync(tracePath), json("checkCRC32" to true)).await()
        val events = requireNotNull(trace.file("trace.trace")) { "Expected browser events in $tracePath" }
        assertContains(events.async("string").await(), "about:blank#teardown")
    }

    @Test
    fun passingScenarioDiscardsItsTrace() = traceSetup.with({ directory -> path.join(directory, "success.zip") }) exercise {
        val scenario = tracedSetup(this) { it } exercise {
            url
        } verify { result ->
            assertEquals("about:blank", result)
        }
        scenario.await()
        existsSync(this)
    } verify { traceExists ->
        assertFalse(traceExists)
    }

    private fun failingScenario(scenario: (PlaywrightSetup<Page>, Throwable) -> Promise<Unit>) = traceSetup.with({ directory ->
        object {
            val tracePath = path.join(directory, "failure.zip")
            val failure = IllegalStateException("scenario failed")
        }
    }) exercise {
        lateinit var openedPage: Page
        val setup = tracedSetup(tracePath) { page ->
            openedPage = page
            page
        }
        val actual = runCatching { scenario(setup, failure).await() }.exceptionOrNull()
        actual to openedPage
    } verify { (actual, page) ->
        assertSame(failure, actual)
        assertTrue(existsSync(tracePath), "Expected trace at $tracePath")
        val closed = runCatching { page.goto("about:blank") }.exceptionOrNull()
        assertContains(closed?.message.orEmpty(), "has been closed")
    }
    private fun tracedSetup(tracePath: String, prepare: suspend (Page) -> Page) = playwrightSetupFrom(
        run = { scenario -> playwrightTest(failureTracePath = tracePath, scenario = scenario) },
    ) { page ->
        asyncSetup.with({ prepare(page) })
    }
}

private external interface FetchResponse {
    val status: Int
}

@JsModule("jszip")
private external val jsZip: ZipReader

private external interface ZipReader {
    fun loadAsync(data: Any?, options: Json): Promise<TraceArchive>
}

private external interface TraceArchive {
    fun file(name: String): TraceEntry?
}

private external interface TraceEntry {
    fun async(type: String): Promise<String>
}
