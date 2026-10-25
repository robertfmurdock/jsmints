package com.zegreatrob.wrapper.wdio

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asPromise
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import org.w3c.dom.url.URL
import kotlin.js.Promise
import kotlin.js.json

object WebdriverBrowser : BrowserLoggingSyntax {

    suspend fun element(selector: String): Element = log(WebdriverBrowser::element) {
        browser.`$`(selector).await()
    }

    suspend fun all(selector: String): Array<Element> = log(
        WebdriverBrowser::all,
    ) { browser.`$$`(selector).await() }

    suspend fun waitUntil(
        condition: suspend () -> Boolean,
        timeout: Int? = null,
        timeoutMessage: String = "",
    ): Unit = log(this::waitUntil.name) {
        val options = json("timeoutMsg" to timeoutMessage)
            .let { if (timeout != null) it.add(json("timeout" to timeout)) else it }
        browser.waitUntil({ MainScope().async { condition() }.asPromise() }, options)
            .await()
    }

    private val baseUrl get() = URL(
        browser.options
            .get("baseUrl").unsafeCast<String>(),
    )

    suspend fun waitForAlert(): Unit = log(WebdriverBrowser::waitForAlert) {
        waitUntil({ isAlertOpen() })
    }

    suspend fun isAlertOpen(): Boolean = log(WebdriverBrowser::isAlertOpen) { browser.isAlertOpen().await() }
    suspend fun acceptAlert(): Unit = log(WebdriverBrowser::acceptAlert) { browser.acceptAlert().await() }
    suspend fun dismissAlert(): Unit = log(WebdriverBrowser::dismissAlert) { browser.dismissAlert().await() }
    suspend fun alertText(): String = log(WebdriverBrowser::alertText) { browser.getAlertText().await() }
    suspend fun currentUrl(): URL = log(WebdriverBrowser::currentUrl) {
        URL(browser.getUrl().await())
    }

    suspend fun refresh() = browser.refresh().await()
    suspend fun setUrl(url: String) = browser.url(url).await()
    suspend fun getLogs() = browser.getLogs("browser").await().toList()

    suspend fun execute(argument: dynamic, arg: (dynamic) -> dynamic): dynamic = browser.execute(arg, argument).await()

    suspend fun setLocation(location: String) {
        val currentUrl = currentUrl()
        when {
            currentUrl.pathname == location -> refresh()
            currentUrl.isNotFromBaseHost() -> setUrl(location)
            else -> alternateImplementation(location)
        }
    }

    private fun URL.isNotFromBaseHost() = hostname != baseUrl.hostname

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private suspend fun alternateImplementation(@Suppress("UNUSED_PARAMETER") location: String) {
        js(
            """
                    browser.execute(function(loc) {
                        if(window.pathSetter){
                            window.pathSetter(loc)
                            return new Promise(function(resolve) {
                                var wait = function() {
                                    window.setTimeout(function() {
                                        if (window.location.pathname.endsWith(loc)) {
                                            resolve()
                                        } else {
                                            wait()
                                        }
                                    }, 5)
                                }
                                wait()
                            })
                        } else {
                            window.location.pathname = loc
                            return Promise.resolve()
                        }
                    }, location);
            """,
        ).unsafeCast<Promise<Unit>>()
            .await()
    }
}
