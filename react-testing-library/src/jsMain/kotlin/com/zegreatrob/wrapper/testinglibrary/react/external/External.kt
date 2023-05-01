package com.zegreatrob.wrapper.testinglibrary.react.external

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import react.ReactNode
import kotlin.js.Promise

@JsModule("@testing-library/react")
@JsNonModule
external val reactTestingLibrary: ReactTestingLibrary

external interface ReactTestingLibrary {
    val screen: Screen
    fun within(element: Element?): Screen
    fun render(node: ReactNode, options: RenderOptions = definedExternally): Result
    fun waitFor(callback: () -> Any?): Promise<Unit>
}

external interface RenderOptions

interface ByTextOptions {
    var selector: String
}

external class Result {
    val container: HTMLElement
}

external class Screen {
    fun getByText(s: String, options: ByTextOptions = definedExternally): HTMLElement
    fun getAllByText(s: String, options: ByTextOptions = definedExternally): Array<HTMLElement>
    fun getByLabelText(s: String): HTMLElement
    fun getAllByLabelText(s: String): Array<HTMLElement>
    fun findByLabelText(s: String): Promise<HTMLElement>
    fun queryByText(s: String): HTMLElement?
    fun queryAllByText(s: String): Array<HTMLElement>
    fun findAllByText(s: String): Promise<Array<HTMLElement>>
    fun findAllByLabelText(s: String): Promise<Array<HTMLElement>>
    fun queryByLabelText(s: String): HTMLElement?
    fun queryAllByLabelText(s: String): Array<HTMLElement>
    fun queryAllByAltText(s: String): Array<HTMLElement>
    fun findByText(text: String): Promise<HTMLElement>
    fun getByRole(role: String, options: TestingLibraryRoleOptions = definedExternally): HTMLElement
    fun getAllByRole(s: String, options: TestingLibraryRoleOptions = definedExternally): Array<HTMLElement>
    fun queryByRole(s: String, options: TestingLibraryRoleOptions = definedExternally): HTMLElement?
    fun queryAllByRole(s: String, options: TestingLibraryRoleOptions = definedExternally): Array<HTMLElement>
    fun findByRole(text: String, options: TestingLibraryRoleOptions = definedExternally): Promise<HTMLElement>
    fun findAllByRole(s: String, options: TestingLibraryRoleOptions): Promise<Array<HTMLElement>>
}

external interface TestingLibraryRoleOptions {
    var name: String
}
