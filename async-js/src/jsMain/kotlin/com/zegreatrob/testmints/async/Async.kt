package com.zegreatrob.testmints.async

import kotlinx.coroutines.*
import kotlin.js.Promise

actual fun <T> testAsync(block: suspend CoroutineScope.() -> T): Any? = GlobalScope.promise(block = block)


actual fun <C : Any> setupAsync2(context: C, additionalActions: suspend C.() -> Unit) = Setup(
        { context },
        MainScope(),
        additionalActions
)

fun <C : Any> setupAsync2(contextProvider: suspend () -> C, additionalActions: suspend C.() -> Unit = {}) = Setup(
        contextProvider,
        MainScope(),
        additionalActions
)

actual fun <C, R, R2> Exercise<C, R>.finalTransform(it: Deferred<R2>): Any? = it.asPromise()