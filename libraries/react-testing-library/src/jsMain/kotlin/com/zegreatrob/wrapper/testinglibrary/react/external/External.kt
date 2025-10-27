package com.zegreatrob.wrapper.testinglibrary.react.external

import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import kotlinx.js.JsPlainObject
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import react.ElementType
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
    fun act(block: () -> Promise<Any?>): Promise<Any?>
    val fireEvent: FireEvent
}

external class FireEvent {
    fun click(element: Element)
    fun submit(element: Element)
}

@JsPlainObject
sealed external interface RenderOptions {
    val wrapper: ElementType<*>
}

@JsPlainObject
sealed external interface ByTextOptions {
    val selector: String
}

external class Result {
    fun rerender(node: ReactNode, options: RenderOptions = definedExternally)
    val container: HTMLElement
    val baseElement: HTMLElement
}

external class Screen {
    fun getByText(s: String, options: ByTextOptions = definedExternally): HTMLElement
    fun getAllByText(s: String, options: ByTextOptions = definedExternally): Array<HTMLElement>
    fun queryByText(s: String): HTMLElement?
    fun queryAllByText(s: String): Array<HTMLElement>
    fun findByText(text: String): Promise<HTMLElement>
    fun findAllByText(s: String): Promise<Array<HTMLElement>>

    fun getByLabelText(s: String): HTMLElement
    fun getAllByLabelText(s: String): Array<HTMLElement>
    fun findByLabelText(s: String): Promise<HTMLElement>
    fun findAllByLabelText(s: String): Promise<Array<HTMLElement>>
    fun queryByLabelText(s: String): HTMLElement?
    fun queryAllByLabelText(s: String): Array<HTMLElement>

    fun getByAltText(s: String): HTMLElement
    fun getAllByAltText(s: String): Array<HTMLElement>
    fun findByAltText(s: String): Promise<HTMLElement>
    fun findAllByAltText(s: String): Promise<Array<HTMLElement>>
    fun queryByAltText(s: String): HTMLElement?
    fun queryAllByAltText(s: String): Array<HTMLElement>

    fun getByTestId(s: String): HTMLElement
    fun getAllByTestId(s: String): Array<HTMLElement>
    fun findByTestId(s: String): Promise<HTMLElement>
    fun findAllByTestId(s: String): Promise<Array<HTMLElement>>
    fun queryByTestId(s: String): HTMLElement?
    fun queryAllByTestId(s: String): Array<HTMLElement>

    fun getByRole(role: String, options: RoleOptions = definedExternally): HTMLElement
    fun getAllByRole(s: String, options: RoleOptions = definedExternally): Array<HTMLElement>
    fun queryByRole(s: String, options: RoleOptions = definedExternally): HTMLElement?
    fun queryAllByRole(s: String, options: RoleOptions = definedExternally): Array<HTMLElement>
    fun findByRole(text: String, options: RoleOptions = definedExternally): Promise<HTMLElement>
    fun findAllByRole(s: String, options: RoleOptions): Promise<Array<HTMLElement>>
}
