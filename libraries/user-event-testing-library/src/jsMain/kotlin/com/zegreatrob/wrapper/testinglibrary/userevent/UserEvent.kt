package com.zegreatrob.wrapper.testinglibrary.userevent

import com.zegreatrob.wrapper.testinglibrary.userevent.external.TLUserEvent
import com.zegreatrob.wrapper.testinglibrary.userevent.external.userEvent
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement

class UserEvent private constructor(private val innerUser: TLUserEvent) {

    suspend fun click(element: HTMLElement?) = innerUser.click(element).await()
    suspend fun type(element: HTMLElement?, text: String) = innerUser.type(element, text).await()
    suspend fun selectOptions(element: HTMLElement, vararg value: String) = innerUser.selectOptions(element, value)
        .await()

    companion object {
        fun setup() = UserEvent(userEvent.default.setup())
    }
}
