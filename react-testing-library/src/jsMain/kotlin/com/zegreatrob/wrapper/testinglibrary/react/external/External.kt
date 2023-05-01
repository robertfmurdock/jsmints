package com.zegreatrob.wrapper.testinglibrary.react.external

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import react.ReactNode
import kotlin.js.Json
import kotlin.js.Promise

@JsModule("@testing-library/react")
@JsNonModule
external val reactTestingLibrary: ReactTestingLibrary

external interface ReactTestingLibrary {
    val screen: Screen
    fun within(element: Element?): Screen
    fun render(node: ReactNode, options: Json = definedExternally): Result

    interface ByTextOptions {
        var selector: String
    }

    fun getByText(element: Element, s: String, options: ByTextOptions = definedExternally): HTMLElement
}

external class Result {
    val container: HTMLElement
}

external class Screen {
    fun getByText(s: String, options: ReactTestingLibrary.ByTextOptions = definedExternally): HTMLElement
    fun getAllByText(s: String, options: ReactTestingLibrary.ByTextOptions = definedExternally): Array<HTMLElement>
    fun getByLabelText(s: String): HTMLElement
    fun queryByText(s: String): HTMLElement?
    fun queryAllByText(s: String): Array<HTMLElement>
    fun findAllByText(s: String): Promise<Array<HTMLElement>>
    fun queryByLabelText(s: String): HTMLElement?
    fun queryAllByAltText(s: String): Array<HTMLElement>
    fun getByRole(role: String, options: Json = definedExternally): HTMLElement
    fun findByText(text: String): Promise<HTMLElement>
}
