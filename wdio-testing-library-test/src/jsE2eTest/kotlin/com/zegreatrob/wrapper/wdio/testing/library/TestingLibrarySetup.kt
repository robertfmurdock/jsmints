package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlin.js.json

internal val fixtureBaseUrl: String
    get() = requireNotNull(js("process.env.BASEURL") as? String) { "BASEURL must be set by the WDIO task" }

val testingLibrarySetup = asyncTestTemplate(beforeAll = {
    WebdriverBrowser.setUrl(fixtureBaseUrl)
})

// These queries exercise already-loaded fixtures with stable missing or duplicate matches.
internal val staticQueryFailureSetup = testingLibrarySetup.extend(
    sharedSetup = { configure(json("asyncUtilTimeout" to 60)) },
    sharedTeardown = { configure(json("asyncUtilTimeout" to 1000)) },
)
