package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlin.js.Promise
import kotlin.test.Test

class BrowserTest {

    @Test
    fun executeAwaitsBrowserContextPromise() = asyncSetup(object : ScopeMint() {
    }) exercise {
        WebdriverBrowser.execute("executed asynchronously") { argument ->
            Promise.resolve<Any?>(argument)
        }
            .unsafeCast<String>()
            .assertIsEqualTo("executed asynchronously")
    }

    @Test
    fun confirmCanCallSetLocationWithoutError() = asyncSetup(object : ScopeMint() {
    }) exercise {
        WebdriverBrowser.setLocation("/there")
    } verify {
        "${WebdriverBrowser.currentUrl()}"
            .assertIsEqualTo("https://static.localhost/there")
    }
}
