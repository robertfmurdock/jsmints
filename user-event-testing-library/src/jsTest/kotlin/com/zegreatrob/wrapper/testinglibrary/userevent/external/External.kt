package com.zegreatrob.wrapper.testinglibrary.userevent.external

import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement
import kotlin.js.Json
import kotlin.js.Promise

@JsModule("@testing-library/user-event")
private external val userEvent: UserEventLib

class UserEvent(private val innerUser: TLUserEvent) {

    suspend fun click(element: HTMLElement?) = innerUser.click(element).await()
    suspend fun type(element: HTMLElement?, text: String) = innerUser.type(element, text).await()
    suspend fun selectOptions(element: HTMLElement, vararg value: String) = innerUser.selectOptions(element, value)
        .await()

    companion object {
        fun setup() = UserEvent(userEvent.default.setup())
    }
}

external interface UserEventLib {
    val default: UserEventLib

    fun setup(json: Json = definedExternally): TLUserEvent
}

external interface TLUserEvent {
    fun click(element: HTMLElement?): Promise<Unit>
    fun type(element: HTMLElement?, text: String): Promise<Unit>
    fun selectOptions(element: HTMLElement, values: Array<out String>): Promise<Unit>
}
