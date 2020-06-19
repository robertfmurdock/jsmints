package com.zegreatrob.testmints.async

import kotlinx.coroutines.Deferred

class Verify<C, R>(private val runTestAsync: (suspend C.(R) -> Unit) -> Deferred<Unit>) {
    infix fun teardown(function: suspend C.(R) -> Unit) = finalTransform { runTestAsync(function) }
}
