@file:JsModule("@testing-library/user-event")
@file:JsNonModule

package com.zegreatrob.react.dataloader.external.testinglibrary.userevent

import org.w3c.dom.Node
import kotlin.js.Promise

@JsName("default")
external val userEvent: UserEvent

external interface UserEvent {
    fun click(
        element: Node?,
        eventInit: dynamic = definedExternally,
        options: dynamic = definedExternally
    ): Promise<Unit>
}
