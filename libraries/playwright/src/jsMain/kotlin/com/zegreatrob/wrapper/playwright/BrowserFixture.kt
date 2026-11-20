package com.zegreatrob.wrapper.playwright

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/** Creates a browser and an isolated context for the scenario. A failing scenario retains its trace if configured. */
suspend fun <T> BrowserType.withPage(
    baseUrl: String? = null,
    headless: Boolean? = null,
    failureTracePath: String? = null,
    test: suspend (Page) -> T,
): T {
    val browser = launch(headless)
    return withCleanup({ browser.close() }) {
        browser.withPageInNewContext(baseUrl, failureTracePath, test = test)
    }
}

internal suspend fun <T> Browser.withPageInNewContext(
    baseUrl: String?,
    failureTracePath: String?,
    configuration: PlaywrightRunConfiguration = PlaywrightRunConfiguration(),
    test: suspend (Page) -> T,
): T {
    val context = newContextWithOptions(configuration.contextOptions(baseUrl))
    suspend fun openPage(): Page {
        configuration.afterContext(context.native)
        return context.newPage().also { configuration.afterPage(it.native) }
    }
    return withCleanup({ context.close() }) {
        if (failureTracePath == null) {
            test(openPage())
        } else {
            context.startTracing()
            val result = try {
                test(openPage())
            } catch (failure: Throwable) {
                withContext(NonCancellable) {
                    runCatching {
                        context.stopTracing(failureTracePath)
                        println("Playwright trace: $failureTracePath")
                    }
                        .exceptionOrNull()?.let(failure::addSuppressed)
                }
                throw failure
            }
            context.stopTracing()
            result
        }
    }
}

internal suspend fun <T> withCleanup(cleanup: suspend () -> Unit, work: suspend () -> T): T {
    var failure: Throwable? = null
    try {
        return work()
    } catch (problem: Throwable) {
        failure = problem
        throw problem
    } finally {
        withContext(NonCancellable) {
            try {
                cleanup()
            } catch (cleanupFailure: Throwable) {
                val original = failure
                if (original == null) throw cleanupFailure
                original.addSuppressed(cleanupFailure)
            }
        }
    }
}
