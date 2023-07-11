package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.wrapper.wdio.WebdriverBrowser

val testingLibrarySetup = asyncTestTemplate(beforeAll = {
    WebdriverBrowser.setUrl("https://static.localhost")
})
