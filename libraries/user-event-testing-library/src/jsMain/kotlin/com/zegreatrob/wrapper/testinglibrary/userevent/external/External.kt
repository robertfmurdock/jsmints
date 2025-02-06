@file:JsModule("@testing-library/user-event")

package com.zegreatrob.wrapper.testinglibrary.userevent.external

import kotlin.js.Json

internal external val userEvent: UserEventLib

internal external interface UserEventLib {
    fun setup(json: Json = definedExternally): TLUserEvent
}
