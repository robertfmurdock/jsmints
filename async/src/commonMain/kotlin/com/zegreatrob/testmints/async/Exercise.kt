package com.zegreatrob.testmints.async

import kotlinx.coroutines.Deferred

class Exercise<C : Any, R>(
    private val runTestAsync: (suspend C.(R) -> Unit) -> (suspend C.(R) -> Unit) -> Deferred<Unit>
) {
    infix fun verify(assertionFunctions: suspend C.(R) -> Unit) = finalTransform {
        runTestAsync(assertionFunctions).invoke {  }
    }

    infix fun verifyAnd(assertionFunctions: suspend C.(R) -> Unit) = Verify(runTestAsync(assertionFunctions))

}
