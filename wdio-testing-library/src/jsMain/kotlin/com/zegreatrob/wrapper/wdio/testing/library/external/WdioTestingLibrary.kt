@file:JsModule("@testing-library/webdriverio")
@file:JsNonModule

package com.zegreatrob.wrapper.wdio.testing.library.external

import com.zegreatrob.wrapper.wdio.Browser
import com.zegreatrob.wrapper.wdio.Element
import kotlin.js.Promise

external fun setupBrowser(browser: Browser): TestingLibraryBrowser

external interface TestingLibraryBrowser {
    fun getByText(text: String): Promise<Element>
    fun getAllByText(text: String): Promise<Array<Element>>
    fun getByLabelText(text: String): Promise<Element>
    fun findByLabelText(text: String): Promise<Element>
    fun findByText(text: String): Promise<Element>
    fun findByDisplayValue(value: String): Promise<Element>
    fun findAllByText(text: String): Promise<Array<Element>>
    fun queryByText(text: String): Promise<Element?>
    fun queryAllByText(text: String): Promise<Array<Element>>
}

external fun within(element: Element): TestingLibraryBrowser
