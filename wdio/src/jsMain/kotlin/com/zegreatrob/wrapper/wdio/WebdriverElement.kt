package com.zegreatrob.wrapper.wdio

import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.json

class WebdriverElement(
    val selector: String = "",
    private val finder: suspend () -> Element = selector.defaultElementFinder()
) : BrowserLoggingSyntax {

    internal suspend fun element() = finder()

    fun all() = WebdriverElementArray(selector)

    fun all(selector: String) = if (this.selector == "")
        WebdriverElementArray {
            element().all(selector).await()
                .map { WebdriverElement { it } }
        }
    else
        WebdriverElementArray("${this.selector} $selector")

    fun element(selector: String): WebdriverElement = if (this.selector == "")
        WebdriverElement {
            element().element(selector).await()
        }
    else
        WebdriverElement("${this.selector} $selector")

    private fun Element.all(selector: String): Promise<Array<Element>> = `$$`(selector)
        .unsafeCast<Promise<Array<Element>>>()

    private fun Element.element(selector: String): Promise<Element> = `$`(selector)
        .unsafeCast<Promise<Element>>()

    suspend fun click(): Unit = log(::click) { element().click().await() }
    suspend fun text(): String = log(::text) { element().getText().await() }
    suspend fun attribute(name: String): String = log(::attribute) { element().getAttribute(name).await() }
    suspend fun isPresent(): Boolean = log(::isPresent) { element().isExisting().await() }
    suspend fun isEnabled(): Boolean = log(::isEnabled) { element().isEnabled().await() }
    suspend fun isDisplayed(): Boolean = log(::isDisplayed) { element().isDisplayed().await() }
    suspend fun isSelected(): Boolean = log(::isSelected) { element().isSelected().await() }
    suspend fun setValue(value: String): Unit = log(::setValue) { element().setValue(value).await() }
    suspend fun clearSetValue(value: String): Unit = log(::clearSetValue) {
        element().clearValue().await()
        element().setValue(value).await()
    }

    suspend fun waitToExist(): Unit = log(::waitToExist) {
        element().waitForExist(json()).await()
    }
}

private fun String.defaultElementFinder(): suspend () -> Element = {
    WebdriverBrowser.element(
        this
    )
}
