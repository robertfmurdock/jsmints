package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.browser
import com.zegreatrob.wrapper.wdio.testing.library.external.TestingLibraryBrowser
import com.zegreatrob.wrapper.wdio.testing.library.external.setupBrowser

object TestingLibraryBrowser : TestingLibraryQueries, BrowserProvider {
    override val extendedWdioBrowser by lazy { setupBrowser(browser) }
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
