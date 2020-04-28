package com.zegreatrob.testmints.async

import kotlinx.coroutines.*

actual fun <T> testAsync(block: suspend CoroutineScope.() -> T): dynamic = GlobalScope.promise(block = block)

actual fun <C, R, R2> Exercise<C, R>.finalTransform(it: Deferred<R2>): dynamic = it.asPromise()
