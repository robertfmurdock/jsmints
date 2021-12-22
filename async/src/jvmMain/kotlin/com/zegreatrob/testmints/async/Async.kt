package com.zegreatrob.testmints.async

@Suppress("RedundantSuspendModifier")
actual suspend fun waitForTest(testFunction: () -> Unit) {
    testFunction()
}

actual fun <T> eventLoopProtect(thing: () -> T) = thing()