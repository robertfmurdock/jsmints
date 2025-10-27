package com.zegreatrob.wrapper.wdio.testing.library

import kotlinx.js.JsPlainObject

@JsPlainObject
sealed external interface RoleOptions {
    var name: String?
}
