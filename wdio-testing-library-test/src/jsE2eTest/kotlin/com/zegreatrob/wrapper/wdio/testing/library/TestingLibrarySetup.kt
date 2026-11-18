package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.wrapper.wdio.WebdriverBrowser

internal val fixtureBaseUrl: String
    get() = requireNotNull(js("process.env.BASEURL") as? String) { "BASEURL must be set by the WDIO task" }

val testingLibrarySetup = asyncTestTemplate(beforeAll = {
    WebdriverBrowser.setUrl(fixtureBaseUrl)
})
