package com.zegreatrob.wrapper.wdio

class WebdriverElementArray(
    val selector: String = "",
    private val finder: suspend () -> List<WebdriverElement> = selector.defaultArrayFinder()
) : BrowserLoggingSyntax {

    private suspend fun all() = finder()

    operator fun get(index: Int) = WebdriverElement { all()[index].innerElement() }

    suspend fun <T> map(transform: suspend (WebdriverElement) -> T) = log("map") {
        all().map { transform(it) }.toList()
    }

    suspend fun count(): Int = log(::count) { all().count() }
    suspend fun first(): WebdriverElement = log(::first) { all().first() }

    suspend fun asList() = map { it }
}

private fun String.defaultArrayFinder(): suspend () -> List<WebdriverElement> = {
    WebdriverBrowser.all(this)
        .map { WebdriverElement { it } }
}
