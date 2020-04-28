package com.zegreatrob.testmints.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking

actual fun <T> testAsync(block: suspend CoroutineScope.() -> T) {
    runBlocking { block() }
}

actual fun <C, R, R2> Exercise<C, R>.finalTransform(it: () -> Deferred<R2>) {
    runBlocking { it().await() }
}

actual suspend fun waitForTest(testFunction: () -> Unit) {
    testFunction()
}

actual fun <T> eventLoopProtect(thing: () -> T) = thing()