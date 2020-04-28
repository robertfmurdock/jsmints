package com.zegreatrob.testmints.async

import kotlinx.coroutines.*
import kotlin.js.Promise

actual fun <T> testAsync(block: suspend CoroutineScope.() -> T): dynamic = GlobalScope.promise(block = block)

actual fun <R2> finalTransform(it: () -> Deferred<R2>): dynamic = it().asPromise()

actual suspend fun waitForTest(testFunction: () -> dynamic) {
    testFunction().unsafeCast<Promise<Unit>>().await()
}

actual fun <T> eventLoopProtect(thing: () -> T) = thing()