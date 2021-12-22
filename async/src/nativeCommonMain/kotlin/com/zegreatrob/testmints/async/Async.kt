package com.zegreatrob.testmints.async

import kotlinx.coroutines.runBlocking

@Suppress("RedundantSuspendModifier")
actual suspend fun waitForTest(testFunction: () -> Unit) {
    testFunction()
}

actual fun <T> eventLoopProtect(thing: () -> T): T = runBlocking { thing() }