package com.zegreatrob.wrapper.playwright

import com.zegreatrob.testmints.async.asyncSetup
import js.array.Tuple
import js.promise.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import node.events.EventEmitter
import node.events.EventType
import node.http.IncomingMessage
import node.http.ServerResponse
import node.http.createServer
import node.net.AddressInfo
import kotlin.js.Promise
import kotlin.js.unsafeCast
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlaywrightConsumerTest {
    @Test
    fun relativeNavigationUsesTheContextBaseUrl() = navigationSetup() exercise {
        val response = page.native.goto(path).await()
        response.asDynamic().status() as Int
    } verify { status ->
        assertEquals(200, status)
        assertEquals(expectedUrl, page.url)
    }

    @Test
    fun aNewScenarioContextDoesNotShareLocalStorage() = navigationSetup {
        page.goto(path)
        val store: (String) -> Promise<Unit> =
            js("(value) => Promise.resolve(localStorage.setItem('jsmints-fixture', value))")
        page.evaluate(store, "original context")
    } exercise {
        withPlaywrightPage(baseUrl = baseUrl) { otherPage ->
            otherPage.goto(path)
            val read: (String) -> Promise<String?> =
                js("(key) => Promise.resolve(localStorage.getItem(key))")
            otherPage.evaluate(read, "jsmints-fixture")
        }
    } verify { storedValue ->
        assertNull(storedValue)
    }

    @Test
    fun nativeFragmentsOverrideContextOptionsAndRunLifecycleHooks() = navigationSetup() exercise {
        page.goto(path)
        val readConfiguration: (Unit) -> Promise<Array<String>> =
            js("() => Promise.resolve([String(innerWidth), document.body.textContent])")
        page.evaluate(readConfiguration, Unit).toList()
    } verify { observed ->
        val profile = js("process.env.JSMINTS_PLAYWRIGHT_PROFILE") as String
        val engine = js("process.env.JSMINTS_PLAYWRIGHT_BROWSER") as String
        val width = if (profile == "firefox") "900" else "1000"
        assertEquals(listOf(width, "$engine:$profile"), observed)
    }

    private fun navigationSetup(
        prepare: suspend NavigationFixture.() -> Unit = {},
    ): PlaywrightSetup<NavigationFixture> {
        lateinit var fixtureBaseUrl: String
        return playwrightSetupFrom(
            run = { scenario ->
                CoroutineScope(Dispatchers.Default).promise {
                    withLocalServer { baseUrl ->
                        fixtureBaseUrl = baseUrl
                        playwrightTest(baseUrl = baseUrl, scenario = scenario).await()
                    }
                }
            },
        ) { page ->
            asyncSetup(NavigationFixture(page, fixtureBaseUrl), additionalActions = prepare)
        }
    }

    private data class NavigationFixture(val page: Page, val baseUrl: String) {
        val path = "/there"
        val expectedUrl = baseUrl + path
    }

    private suspend fun <T> withLocalServer(test: suspend (String) -> T): T {
        val host = "127.0.0.1"
        val server = createServer<IncomingMessage, ServerResponse<IncomingMessage>> { request, response ->
            val browserHeader = request.headers.asDynamic()["x-jsmints-browser"] as? String
            val profileHeader = request.headers.asDynamic()["x-jsmints-profile"] as? String
            response.end("$browserHeader:$profileHeader")
        }
        val listening = EventEmitter.once<Tuple>(server, EventType("listening"))
        server.listen(0, host)
        listening.await()
        try {
            val port = server.address().unsafeCast<AddressInfo>().port
            return test("http://$host:$port")
        } finally {
            val closed = EventEmitter.once<Tuple>(server, EventType("close"))
            server.close()
            closed.await()
        }
    }
}
