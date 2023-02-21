package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlin.test.Test

class BrowserTest {

    @Test
    fun confirmCanCallSetLocationWithoutError() = asyncSetup(object : ScopeMint() {
    }) exercise {
        WebdriverBrowser.setLocation("/there")
    } verify {
        "${WebdriverBrowser.currentUrl()}"
            .assertIsEqualTo("https://static.localhost/there")
    }
}
