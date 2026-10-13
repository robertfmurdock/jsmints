package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.browser
import com.zegreatrob.wrapper.wdio.testing.library.external.TestingLibraryBrowser
import com.zegreatrob.wrapper.wdio.testing.library.external.setupBrowser
import kotlin.js.json

object TestingLibraryBrowser : TestingLibraryQueries, BrowserProvider {
    override val extendedWdioBrowser by lazy { setupBrowserWithWebdriverIo10Compatibility(browser) }
}

private fun setupBrowserWithWebdriverIo10Compatibility(browser: com.zegreatrob.wrapper.wdio.Browser): TestingLibraryBrowser {
    val dynamicBrowser = browser.asDynamic()
    val addCommand = dynamicBrowser.addCommand

    dynamicBrowser.addCommand = { name: String, command: dynamic, options: dynamic ->
        addCommand.call(
            dynamicBrowser,
            name,
            command,
            if (options == true) json("attachToElement" to true) else options,
        )
    }

    return setupBrowser(browser)
}

interface BrowserProvider {
    val extendedWdioBrowser: TestingLibraryBrowser
}

interface TestingLibraryQueries :
    ByRole,
    ByText,
    ByLabelText,
    Within

interface Within {
    suspend fun within(element: WebdriverElement): TestingLibraryQueries {
        val innerElement = element.innerElement()
        val within = com.zegreatrob.wrapper.wdio.testing.library.external.within(innerElement)
        return object : TestingLibraryQueries {
            override val extendedWdioBrowser: TestingLibraryBrowser = within
        }
    }
}
