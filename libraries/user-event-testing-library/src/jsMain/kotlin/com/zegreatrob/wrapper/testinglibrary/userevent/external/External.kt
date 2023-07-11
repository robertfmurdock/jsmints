package com.zegreatrob.wrapper.testinglibrary.userevent.external

import org.w3c.dom.HTMLElement
import kotlin.js.Json
import kotlin.js.Promise

@JsModule("@testing-library/user-event")
internal external val userEvent: UserEventLib

internal external interface UserEventLib {
    val default: UserEventLib

    fun setup(json: Json = definedExternally): TLUserEvent
}

internal external interface TLUserEvent {
    fun click(element: HTMLElement?): Promise<Unit>
    fun type(element: HTMLElement?, text: String): Promise<Unit>
    fun selectOptions(element: HTMLElement, values: Array<out String>): Promise<Unit>
}
