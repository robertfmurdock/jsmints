package com.zegreatrob.wrapper.wdio

import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.json

class WebdriverElement(
    val selector: String = "",
    private val finder: suspend () -> Element = selector.defaultElementFinder()
) : BrowserLoggingSyntax {

    suspend fun innerElement() = finder()

    fun all() = WebdriverElementArray(selector)

    fun all(selector: String) = if (this.selector == "")
        WebdriverElementArray {
            innerElement().all(selector).await()
                .map { WebdriverElement { it } }
        }
    else
        WebdriverElementArray("${this.selector} $selector")

    fun element(selector: String): WebdriverElement = if (this.selector == "")
        WebdriverElement {
            innerElement().element(selector).await()
        }
    else
        WebdriverElement("${this.selector} $selector")

    private fun Element.all(selector: String): Promise<Array<Element>> = `$$`(selector)
        .unsafeCast<Promise<Array<Element>>>()

    private fun Element.element(selector: String): Promise<Element> = `$`(selector)
        .unsafeCast<Promise<Element>>()

    suspend fun click(): Unit = log(::click) { innerElement().click().await() }
    suspend fun dragAndDrop(element: WebdriverElement): Unit = log(::dragAndDrop) {
        innerElement().dragAndDrop(element.innerElement()).await()
    }

    suspend fun text(): String = log(::text) { innerElement().getText().await() }
    suspend fun attribute(name: String): String = log(::attribute) { innerElement().getAttribute(name).await() }
    suspend fun isPresent(): Boolean = log(::isPresent) { innerElement().isExisting().await() }
    suspend fun isEnabled(): Boolean = log(::isEnabled) { innerElement().isEnabled().await() }
    suspend fun isDisplayed(): Boolean = log(::isDisplayed) { innerElement().isDisplayed().await() }
    suspend fun isSelected(): Boolean = log(::isSelected) { innerElement().isSelected().await() }
    suspend fun selectByIndex(index: Int): Unit = log(::selectByIndex) { innerElement().selectByIndex(index).await() }
    suspend fun parentElement(): WebdriverElement = log(::selectByIndex) {
        WebdriverElement(finder = {
            innerElement().parentElement().await()
        })
    }

    suspend fun nextElement(): WebdriverElement = log(::selectByIndex) {
        WebdriverElement(finder = {
            innerElement().nextElement().await()
        })
    }

    suspend fun previousElement(): WebdriverElement = log(::selectByIndex) {
        WebdriverElement(finder = {
            innerElement().previousElement().await()
        })
    }

    suspend fun selectByVisibleText(text: String): Unit =
        log(::selectByVisibleText) { innerElement().selectByVisibleText(text).await() }

    suspend fun setValue(value: String): Unit = log(::setValue) { innerElement().setValue(value).await() }
    suspend fun clearSetValue(value: String): Unit = log(::clearSetValue) {
        innerElement().clearValue().await()
        innerElement().setValue(value).await()
    }

    suspend fun waitToExist(): Unit = log(::waitToExist) {
        innerElement().waitForExist(json()).await()
    }
}

private fun String.defaultElementFinder(): suspend () -> Element = {
    WebdriverBrowser.element(
        this
    )
}
