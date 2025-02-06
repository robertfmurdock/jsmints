package com.zegreatrob.wrapper.testinglibrary.userevent.external

import org.w3c.dom.HTMLElement
import kotlin.js.Promise

internal external interface TLUserEvent {
    fun click(element: HTMLElement?): Promise<Unit>
    fun type(element: HTMLElement?, text: String): Promise<Unit>
    fun selectOptions(element: HTMLElement, values: Array<out String>): Promise<Unit>
}
