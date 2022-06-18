@file:JsModule("@testing-library/react")
@file:JsNonModule

package com.zegreatrob.minreact.external.testinglibrary.react

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import react.ReactNode
import kotlin.js.Json

external val screen: Screen

external fun render(node: ReactNode, options: Json = definedExternally): Result

external class Result {
    val container: HTMLElement
}

external interface ByTextOptions {
    var selector: String
}

external fun getByText(element: Element, s: String, options: ByTextOptions = definedExternally): HTMLElement

external class Screen {
    fun getByText(s: String, options: ByTextOptions = definedExternally): HTMLElement
}
