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
    val engine = configuredEngine()
    val browser = configuredBrowserType()
    val headless = (NodeProcess.env["JSMINTS_PLAYWRIGHT_HEADLESS"] as? String)?.toBooleanStrict()
    val configuration = readPlaywrightRunConfiguration(
        directory = NodeProcess.env["JSMINTS_PLAYWRIGHT_CONFIG_DIRECTORY"] as? String,
        profile = NodeProcess.env["JSMINTS_PLAYWRIGHT_PROFILE"] as? String ?: engine,
        browser = engine,
    )
    suspend fun launch(): Browser {
        val launched = browser.launchWithOptions(configuration.launchOptions(headless))
        try {
            configuration.afterLaunch(launched.native)
        } catch (failure: Throwable) {
            withCleanup({ launched.close() }) { throw failure }
        }
        return launched
    }
    if ((NodeProcess.env["JSMINTS_PLAYWRIGHT_REUSE_BROWSER"] as? String) == "true") {
        return runBrowser.acquire(::launch).withPageInNewContext(baseUrl, failureTracePath, configuration, test)
    }
    val launched = launch()
    return withCleanup({ launched.close() }) {
        launched.withPageInNewContext(baseUrl, failureTracePath, configuration, test)
    }
}

private fun configuredEngine() = NodeProcess.env["JSMINTS_PLAYWRIGHT_BROWSER"] as? String ?: "chromium"

internal fun configuredBrowserType() = when (val engine = configuredEngine()) {
    "chromium" -> Playwright.chromium
    "firefox" -> Playwright.firefox
    "webkit" -> Playwright.webkit
    else -> error("Unsupported Playwright browser: $engine")
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
