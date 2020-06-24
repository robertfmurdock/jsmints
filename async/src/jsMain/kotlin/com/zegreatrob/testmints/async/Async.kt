package com.zegreatrob.testmints.async

import kotlinx.coroutines.*
import kotlin.js.Promise

@Suppress("unused")
actual fun <T> testAsync(block: suspend CoroutineScope.() -> T): dynamic = GlobalScope.promise(block = block)

actual fun finalTransform(it: () -> Deferred<Unit>): dynamic = it().asPromise()

actual suspend fun waitForTest(testFunction: () -> dynamic) {
    testFunction().unsafeCast<Promise<Unit>>().await()
}

actual fun <T> eventLoopProtect(thing: () -> T) = thing()