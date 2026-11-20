package com.zegreatrob.wrapper.playwright

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.promise
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.js.Promise

internal val runBrowser by lazy {
    val browser = RunBrowser()
    val afterAll = js("globalThis.after").unsafeCast<((() -> Promise<Unit>) -> Unit)?>()
    checkNotNull(afterAll) { "Playwright browser reuse requires a Mocha run. Set playwrightReuseBrowser=false for other runners." }
    afterAll {
        CoroutineScope(Dispatchers.Default).promise { browser.close() }
    }
    browser
}

internal class RunBrowser {
    private val lock = Mutex()
    private var browser: Browser? = null

    suspend fun acquire(launch: suspend () -> Browser): Browser = lock.withLock {
        browser?.takeIf { it.isConnected } ?: launch().also { browser = it }
    }

    suspend fun close() = lock.withLock {
        val current = browser
        browser = null
        current?.close()
        Unit
    }
}
