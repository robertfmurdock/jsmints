@file:JsModule("@testing-library/react")
@file:JsNonModule

package com.zegreatrob.react.dataloader.external.testinglibrary.react

import org.w3c.dom.HTMLElement
import react.ReactNode
import kotlin.js.Json
import kotlin.js.Promise

external val screen: Screen

external fun render(node: ReactNode, options: Json = definedExternally): Result

external class Result {
    val container: HTMLElement
}

external interface ByTextOptions {
    var selector: String
}

external class Screen {
    fun getByText(s: String, options: ByTextOptions = definedExternally): HTMLElement
    fun findByText(s: String, options: ByTextOptions = definedExternally): Promise<HTMLElement>
}

external fun waitFor(callback: () -> Any): Promise<Unit>
