package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.test.Test

class PublicHttpsFetchTest {

    @Test
    fun fetchesAPublicHttpsEndpointFromTheWdioPluginWorker() = asyncSetup(object {
    }) exercise {
        fetchFromWdioWorker("https://example.com").await().status
    } verify { status ->
        status.assertIsEqualTo(200)
    }
}

private external interface FetchResponse {
    val status: Int
}

private fun fetchFromWdioWorker(url: String): Promise<FetchResponse> = js("globalThis.fetch(url)").unsafeCast<Promise<FetchResponse>>()
