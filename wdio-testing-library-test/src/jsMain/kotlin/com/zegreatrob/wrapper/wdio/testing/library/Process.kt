@file:Suppress("unused")

package com.zegreatrob.wrapper.wdio.testing.library

import kotlin.js.Json

external val process: Process

external interface Process {

    val env: Json

    fun exit()
    fun exit(code: Int)
}
