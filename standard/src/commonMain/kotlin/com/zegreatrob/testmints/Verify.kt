package com.zegreatrob.testmints

class Verify<C, R>(private val runTest: (C.(R) -> Unit) -> Unit) {
    infix fun teardown(teardownFunctions: C.(R) -> Unit) = runTest(teardownFunctions)
}
