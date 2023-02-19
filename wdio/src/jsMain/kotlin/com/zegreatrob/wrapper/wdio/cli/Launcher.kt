package com.zegreatrob.wrapper.wdio.cli

import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.Promise

external interface Launcher {
    fun run(): Promise<Int>
}

@Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
suspend fun Launcher(configPath: String, options: Json): Launcher {
    val constructor = getConstructor()
    println("he hee")
    val cp = configPath
    val o = options
    return js("new constructor(cp, o)").unsafeCast<Launcher>()
}

private suspend fun getConstructor(): dynamic = Promise.resolve<Json>(js("import(\"@wdio/cli\")"))
    .then {
        println("hi hi")
        it["Launcher"].asDynamic()
    }
    .await()
