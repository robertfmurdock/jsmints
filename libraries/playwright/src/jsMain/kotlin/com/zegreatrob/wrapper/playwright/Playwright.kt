package com.zegreatrob.wrapper.playwright

import kotlinx.coroutines.await
import kotlin.js.JsModule
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

object Playwright {
    val chromium: BrowserType get() = BrowserType(PlaywrightModule.chromium)
    val firefox: BrowserType get() = BrowserType(PlaywrightModule.firefox)
}

class BrowserType internal constructor(private val delegate: JsBrowserType) {
    suspend fun launch(headless: Boolean? = null): Browser {
        val options = json()
        headless?.let { options["headless"] = it }
        return Browser(delegate.launch(options).await())
    }
}

class Browser internal constructor(private val delegate: JsBrowser) {
    suspend fun newContext(baseUrl: String? = null): BrowserContext {
        val options = json()
        baseUrl?.let { options["baseURL"] = it }
        return BrowserContext(delegate.newContext(options).await())
    }

    suspend fun close() {
        delegate.close().await()
    }
}

class BrowserContext internal constructor(private val delegate: JsBrowserContext) {
    suspend fun startTracing() {
        delegate.tracing.start(json("screenshots" to true, "snapshots" to true)).await()
    }

    suspend fun stopTracing(path: String? = null) {
        val options = json()
        path?.let { options["path"] = it }
        delegate.tracing.stop(options).await()
    }

    suspend fun newPage(): Page = Page(delegate.newPage().await())

    suspend fun close() {
        delegate.close().await()
    }
}

class Page internal constructor(private val delegate: JsPage) {
    val url: String get() = delegate.url()

    suspend fun goto(url: String) {
        delegate.goto(url).await()
    }

    fun locator(selector: String): Locator = Locator(delegate.locator(selector))

    fun getByRole(
        role: String,
        name: String? = null,
        exact: Boolean? = null,
    ): Locator = Locator(delegate.getByRole(role, roleOptions(name, exact)))

    fun getByText(text: String, exact: Boolean? = null): Locator = Locator(delegate.getByText(text, matchOptions(exact)))

    fun getByLabel(text: String, exact: Boolean? = null): Locator = Locator(delegate.getByLabel(text, matchOptions(exact)))

    /**
     * Evaluates a function in the page with a serializable argument and awaits its result.
     * The function is serialized, so it must use browser globals and its argument
     * without capturing Node values or referencing the Kotlin runtime.
     */
    suspend fun <A, R> evaluate(
        pageFunction: (A) -> Promise<R>,
        argument: A,
    ): R = delegate.evaluate<A, R>(pageFunction, argument).await()
}

class Locator internal constructor(private val delegate: JsLocator) {
    fun getByRole(
        role: String,
        name: String? = null,
        exact: Boolean? = null,
    ): Locator = Locator(delegate.getByRole(role, roleOptions(name, exact)))

    fun getByText(text: String, exact: Boolean? = null): Locator = Locator(delegate.getByText(text, matchOptions(exact)))

    fun getByLabel(text: String, exact: Boolean? = null): Locator = Locator(delegate.getByLabel(text, matchOptions(exact)))

    fun first(): Locator = Locator(delegate.first())

    suspend fun count(): Int = delegate.count().await()

    /** Returns locators for the matches currently present, in document order. */
    suspend fun all(): List<Locator> = delegate.all().await().map(::Locator)

    suspend fun getAttribute(name: String): String? = delegate.getAttribute(name).await()

    suspend fun isVisible(): Boolean = delegate.isVisible().await()

    suspend fun waitForVisible(timeoutMillis: Int) = waitFor("visible", timeoutMillis)

    suspend fun waitForAttached(timeoutMillis: Int) = waitFor("attached", timeoutMillis)

    private suspend fun waitFor(state: String, timeoutMillis: Int) {
        require(timeoutMillis > 0) { "Timeout must be positive" }
        delegate.waitFor(json("state" to state, "timeout" to timeoutMillis)).await()
    }

    suspend fun click() {
        delegate.click().await()
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
}

internal external interface JsBrowserType {
    fun launch(options: Json): Promise<JsBrowser>
}

internal external interface JsBrowser {
    fun newContext(options: Json): Promise<JsBrowserContext>
    fun close(): Promise<Unit>
}

internal external interface JsBrowserContext {
    val tracing: JsTracing
    fun newPage(): Promise<JsPage>
    fun close(): Promise<Unit>
}

internal external interface JsTracing {
    fun start(options: Json): Promise<Unit>
    fun stop(options: Json): Promise<Unit>
}

internal external interface JsPage {
    fun goto(url: String): Promise<Any?>
    fun url(): String
    fun locator(selector: String): JsLocator
    fun getByRole(role: String, options: Json): JsLocator
    fun getByText(text: String, options: Json): JsLocator
    fun getByLabel(text: String, options: Json): JsLocator
    fun <A, R> evaluate(pageFunction: (A) -> Promise<R>, argument: A): Promise<R>
}

internal external interface JsLocator {
    fun getByRole(role: String, options: Json): JsLocator
    fun getByText(text: String, options: Json): JsLocator
    fun getByLabel(text: String, options: Json): JsLocator
    fun first(): JsLocator
    fun count(): Promise<Int>
    fun all(): Promise<Array<JsLocator>>
    fun getAttribute(name: String): Promise<String?>
    fun isVisible(): Promise<Boolean>
    fun waitFor(options: Json): Promise<Unit>
    fun click(): Promise<Unit>
}
