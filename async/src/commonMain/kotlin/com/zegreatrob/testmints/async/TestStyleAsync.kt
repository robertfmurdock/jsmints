package com.zegreatrob.testmints.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

expect fun <T> testAsync(block: suspend CoroutineScope.() -> T)

expect fun finalTransform(it: () -> Deferred<Unit>)

expect suspend fun waitForTest(testFunction: () -> Unit)

expect fun <T> eventLoopProtect(thing: () -> T): T
