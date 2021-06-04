package com.zegreatrob.wrapper.wdio

import kotlinx.coroutines.*
import org.w3c.dom.url.URL
import kotlin.js.Promise
import kotlin.js.json

object WebdriverBrowser : BrowserLoggingSyntax {

    suspend fun element(selector: String): Element = log(WebdriverBrowser::element) {
        browser.`$`(selector).await()
    }

    suspend fun all(selector: String): Array<Element> = log(
        WebdriverBrowser::all
    ) { browser.`$$`(selector).await() }

    suspend fun waitUntil(
        condition: suspend () -> Boolean,
        timeout: Int? = null,
        timeoutMessage: String = ""
    ): Unit = log(this::waitUntil.name) {
        val options = json("timeoutMsg" to timeoutMessage)
            .let { if (timeout != null) it.add(json("timeout" to timeout)) else it }
        browser.waitUntil({ MainScope().async { condition() }.asPromise() }, options)
            .await()
    }

    private val baseUrl get() = URL(browser.config["baseUrl"].unsafeCast<String>())

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

    suspend fun executeAsync(argument: dynamic, arg: (dynamic, () -> Unit) -> dynamic) =
        browser.executeAsync(arg, argument).await()

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
                    browser.executeAsync(function(loc, done) {
                        var wait = function() {
                            window.setTimeout(function() {
                                if (window.location.pathname.contains(loc)) {
                                    done()
                                } else {
                                    wait()
                                }
                            }, 5)
                        }
                        
                        if(window.pathSetter){
                            window.pathSetter(loc)
                            wait()
                        } else {
                            done()
                            window.location.pathname = loc
                        }
                        }, location);
                    """
        ).unsafeCast<Promise<Unit>>()
            .await()
    }

}
