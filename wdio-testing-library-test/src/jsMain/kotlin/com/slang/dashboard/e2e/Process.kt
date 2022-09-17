@file:Suppress("unused")

package com.slang.dashboard.e2e

import kotlin.js.Json

external val process: Process

external interface Process {

    val env: Json

    fun exit()
    fun exit(code: Int)
}
