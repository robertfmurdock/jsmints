package com.zegreatrob.testmints.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking

@Suppress("unused")
actual fun <T> testAsync(block: suspend CoroutineScope.() -> T) {
    runBlocking { block() }
}

actual fun finalTransform(it: () -> Deferred<Unit>) {
    runBlocking { it().await() }
}

@Suppress("RedundantSuspendModifier")
actual suspend fun waitForTest(testFunction: () -> Unit) {
    testFunction()
}

actual fun <T> eventLoopProtect(thing: () -> T): T = runBlocking { thing() }