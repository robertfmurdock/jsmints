package com.zegreatrob.testmints.async

import kotlinx.coroutines.*

expect fun <T> testAsync(block: suspend CoroutineScope.() -> T)

suspend fun <C> setupAsync(context: C, additionalSetup: suspend C.() -> Unit = {}) = SetupAsync(context).apply { additionalSetup(context) }

expect fun <R2> finalTransform(it: () -> Deferred<R2>)

expect suspend fun waitForTest(testFunction: () -> Unit)

expect fun <T> eventLoopProtect(thing: () -> T): T
