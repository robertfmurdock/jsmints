package com.zegreatrob.wrapper.playwright

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlinx.coroutines.test.TestResult
import kotlin.js.JsModule
import kotlin.js.Promise

/** Runs the complete test result before closing the page, including asynchronous assertions and teardown. */
fun playwrightTest(
    baseUrl: String? = NodeProcess.env["BASEURL"] as? String,
    failureTracePath: String? = configuredTracePath(),
    scenario: (Page) -> TestResult,
): Promise<Unit> = CoroutineScope(Dispatchers.Default).promise {
    withPlaywrightPage(baseUrl, failureTracePath) { page ->
        scenario(page).unsafeCast<Promise<Unit>>().await()
    }
}

/** Uses the Gradle test run's browser, headless, base URL, and trace settings. */
suspend fun <T> withPlaywrightPage(
    baseUrl: String? = NodeProcess.env["BASEURL"] as? String,
    failureTracePath: String? = configuredTracePath(),
    test: suspend (Page) -> T,
): T {
    val browser = when (val engine = NodeProcess.env["JSMINTS_PLAYWRIGHT_BROWSER"] as? String) {
        null, "chromium" -> Playwright.chromium
        "firefox" -> Playwright.firefox
        else -> error("Unsupported Playwright browser: $engine")
    }
    val headless = (NodeProcess.env["JSMINTS_PLAYWRIGHT_HEADLESS"] as? String)?.toBooleanStrict()
    return browser.withPage(baseUrl, headless, failureTracePath, test)
}

private fun configuredTracePath() = (NodeProcess.env["JSMINTS_PLAYWRIGHT_TRACE_DIRECTORY"] as? String)?.let {
    NodePath.join(it, "${NodeCrypto.randomUUID()}.zip")
}

@JsModule("node:process")
private external object NodeProcess {
    val env: kotlin.js.Json
}

@JsModule("node:path")
private external object NodePath {
    fun join(directory: String, filename: String): String
}

@JsModule("node:crypto")
private external object NodeCrypto {
    fun randomUUID(): String
}
