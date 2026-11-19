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
import kotlin.js.unsafeCast
import kotlin.test.Test
import kotlin.test.assertEquals

class PlaywrightConsumerTest {
    @Test
    fun relativeNavigationUsesTheContextBaseUrl() = navigationSetup exercise {
        page.goto(path)
    } verify {
        assertEquals(expectedUrl, page.url)
    }

    private val navigationSetup get(): PlaywrightSetup<NavigationFixture> {
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
            asyncSetup(NavigationFixture(page, fixtureBaseUrl))
        }
    }

    private data class NavigationFixture(val page: Page, val baseUrl: String) {
        val path = "/there"
        val expectedUrl = baseUrl + path
    }

    private suspend fun <T> withLocalServer(test: suspend (String) -> T): T {
        val host = "127.0.0.1"
        val server = createServer<IncomingMessage, ServerResponse<IncomingMessage>> { _, response ->
            response.end("Playwright navigation fixture")
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
