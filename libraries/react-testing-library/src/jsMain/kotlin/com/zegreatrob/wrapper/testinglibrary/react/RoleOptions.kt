package com.zegreatrob.wrapper.testinglibrary.react

import kotlinx.js.JsPlainObject

@JsPlainObject
sealed external interface RoleOptions {
    var name: String?
    var selected: Boolean?
}
