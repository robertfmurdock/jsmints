package com.zegreatrob.wrapper.playwright

import kotlinx.coroutines.await
import kotlin.js.JsModule
import kotlin.js.JsName
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.definedExternally
import kotlin.js.json

object Playwright {
    val chromium: BrowserType get() = BrowserType(PlaywrightModule.chromium)
    val firefox: BrowserType get() = BrowserType(PlaywrightModule.firefox)
    val webkit: BrowserType get() = BrowserType(PlaywrightModule.webkit)
}

class BrowserType internal constructor(val native: JsBrowserType) {
    suspend fun launch(headless: Boolean? = null): Browser {
        val options = json()
        headless?.let { options["headless"] = it }
        return launchWithOptions(options)
    }

    internal suspend fun launchWithOptions(options: Json): Browser = Browser(native.launch(options).await())
}

class Browser internal constructor(val native: JsBrowser) {
    internal val isConnected: Boolean get() = native.isConnected()

    suspend fun newContext(baseUrl: String? = null): BrowserContext {
        val options = json()
        baseUrl?.let { options["baseURL"] = it }
        return newContextWithOptions(options)
    }

    internal suspend fun newContextWithOptions(options: Json): BrowserContext = BrowserContext(native.newContext(options).await())

    suspend fun close() {
        native.close().await()
    }
}

class BrowserContext internal constructor(val native: JsBrowserContext) {
    suspend fun startTracing() {
        native.tracing.start(json("screenshots" to true, "snapshots" to true)).await()
    }

    suspend fun stopTracing(path: String? = null) {
        val options = json()
        path?.let { options["path"] = it }
        native.tracing.stop(options).await()
    }

    suspend fun newPage(): Page = Page(native.newPage().await())

    suspend fun close() {
        native.close().await()
    }
}

class Page internal constructor(val native: JsPage) {
    val url: String get() = native.url()

    suspend fun goto(url: String) {
        native.goto(url).await()
    }

    fun locator(selector: String): Locator = Locator(native.locator(selector))

    fun getByRole(
        role: String,
        name: String? = null,
        exact: Boolean? = null,
    ): Locator = Locator(native.getByRole(role, roleOptions(name, exact)))

    fun getByText(text: String, exact: Boolean? = null): Locator = Locator(native.getByText(text, matchOptions(exact)))

    fun getByLabel(text: String, exact: Boolean? = null): Locator = Locator(native.getByLabel(text, matchOptions(exact)))

    /**
     * Evaluates a function in the page with a serializable argument and awaits its result.
     * The function is serialized, so it must use browser globals and its argument
     * without capturing Node values or referencing the Kotlin runtime.
     */
    suspend fun <A, R> evaluate(
        pageFunction: (A) -> Promise<R>,
        argument: A,
    ): R = native.evaluateAsync(pageFunction, argument).await()
}

class Locator internal constructor(val native: JsLocator) {
    fun getByRole(
        role: String,
        name: String? = null,
        exact: Boolean? = null,
    ): Locator = Locator(native.getByRole(role, roleOptions(name, exact)))

    fun getByText(text: String, exact: Boolean? = null): Locator = Locator(native.getByText(text, matchOptions(exact)))

    fun getByLabel(text: String, exact: Boolean? = null): Locator = Locator(native.getByLabel(text, matchOptions(exact)))

    fun first(): Locator = Locator(native.first())

    suspend fun count(): Int = native.count().await()

    /** Returns locators for the matches currently present, in document order. */
    suspend fun all(): List<Locator> = native.all().await().map(::Locator)

    suspend fun getAttribute(name: String): String? = native.getAttribute(name).await()

    suspend fun isVisible(): Boolean = native.isVisible().await()

    suspend fun waitForVisible(timeoutMillis: Int) = waitFor("visible", timeoutMillis)

    suspend fun waitForAttached(timeoutMillis: Int) = waitFor("attached", timeoutMillis)

    private suspend fun waitFor(state: String, timeoutMillis: Int) {
        require(timeoutMillis > 0) { "Timeout must be positive" }
        native.waitFor(json("state" to state, "timeout" to timeoutMillis)).await()
    }

    suspend fun click() {
        native.click().await()
    }
}

private fun roleOptions(name: String?, exact: Boolean?) = matchOptions(exact).apply {
    name?.let { this["name"] = it }
}

private fun matchOptions(exact: Boolean?) = json().apply {
    exact?.let { this["exact"] = it }
}

@JsModule("playwright")
private external object PlaywrightModule {
    val chromium: JsBrowserType
    val firefox: JsBrowserType
    val webkit: JsBrowserType
}

external interface JsBrowserType {
    fun launch(options: Json = definedExternally): Promise<JsBrowser>
}

external interface JsBrowser {
    fun isConnected(): Boolean
    fun newContext(options: Json = definedExternally): Promise<JsBrowserContext>
    fun close(options: Json = definedExternally): Promise<Unit>
}

external interface JsBrowserContext {
    val tracing: JsTracing
    fun newPage(): Promise<JsPage>
    fun close(options: Json = definedExternally): Promise<Unit>
}

external interface JsTracing {
    fun start(options: Json = definedExternally): Promise<Unit>
    fun stop(options: Json = definedExternally): Promise<Unit>
}

external interface JsPage {
    fun goto(url: String, options: Json = definedExternally): Promise<Any?>
    fun url(): String
    fun locator(selector: String, options: Json = definedExternally): JsLocator
    fun getByRole(role: String, options: Json = definedExternally): JsLocator
    fun getByText(text: String, options: Json = definedExternally): JsLocator
    fun getByLabel(text: String, options: Json = definedExternally): JsLocator
    fun <R> evaluate(pageFunction: () -> R): Promise<R>
    fun <A, R> evaluate(pageFunction: (A) -> R, argument: A): Promise<R>
    fun <R> evaluate(pageFunction: String, argument: Any? = definedExternally): Promise<R>

    @JsName("evaluate")
    fun <R> evaluateAsync(pageFunction: () -> Promise<R>): Promise<R>

    @JsName("evaluate")
    fun <A, R> evaluateAsync(pageFunction: (A) -> Promise<R>, argument: A): Promise<R>
}

external interface JsLocator {
    fun getByRole(role: String, options: Json = definedExternally): JsLocator
    fun getByText(text: String, options: Json = definedExternally): JsLocator
    fun getByLabel(text: String, options: Json = definedExternally): JsLocator
    fun first(): JsLocator
    fun count(): Promise<Int>
    fun all(): Promise<Array<JsLocator>>
    fun getAttribute(name: String, options: Json = definedExternally): Promise<String?>
    fun isVisible(options: Json = definedExternally): Promise<Boolean>
    fun waitFor(options: Json = definedExternally): Promise<Unit>
    fun click(options: Json = definedExternally): Promise<Unit>
}
