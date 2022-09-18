package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.wrapper.wdio.browser
import com.zegreatrob.wrapper.wdio.testing.library.external.TestingLibraryBrowser
import com.zegreatrob.wrapper.wdio.testing.library.external.setupBrowser

object TestingLibraryBrowser : ByRole, ByText, BrowserProvider {
    override val extendedWdioBrowser by lazy { setupBrowser(browser) }
}

interface BrowserProvider {
    val extendedWdioBrowser: TestingLibraryBrowser
}
