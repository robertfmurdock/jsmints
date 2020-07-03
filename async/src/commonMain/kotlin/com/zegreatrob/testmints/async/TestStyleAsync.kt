package com.zegreatrob.testmints.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

expect fun <T> testAsync(block: suspend CoroutineScope.() -> T)

@Deprecated(
    "setupAsync is now deprecated and will be removed in future versions.",
    replaceWith = ReplaceWith("asyncSetup")
)
suspend fun <C> setupAsync(context: C, additionalSetup: suspend C.() -> Unit = {}) = SetupAsync(context)
    .apply { additionalSetup(context) }

expect fun finalTransform(it: () -> Deferred<Unit>)

expect suspend fun waitForTest(testFunction: () -> Unit)

expect fun <T> eventLoopProtect(thing: () -> T): T
