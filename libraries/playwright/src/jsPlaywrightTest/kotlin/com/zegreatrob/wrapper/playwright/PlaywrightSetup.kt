package com.zegreatrob.wrapper.playwright

internal fun documentSetup(html: String) = playwrightSetup { page ->
    page.loadDocument(html)
    page
}

internal suspend fun Page.loadDocument(html: String) {
    goto("data:text/html,${encodeURIComponent(html)}")
}

private external fun encodeURIComponent(value: String): String
