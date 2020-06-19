package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.CompoundMintTestException
import com.zegreatrob.testmints.captureException
import com.zegreatrob.testmints.report.MintReporter
import kotlinx.coroutines.*

class Exercise<C : Any, R>(
        private val runTestAsync: (suspend C.(R) -> Unit) -> (suspend C.(R) -> Unit) -> Deferred<Unit>
) {
    infix fun verify(assertionFunctions: suspend C.(R) -> Unit) = finalTransform {
        runTestAsync { }(assertionFunctions)
    }

    infix fun verifyAnd(assertionFunctions: suspend C.(R) -> Unit) = Verify(runTestAsync(assertionFunctions))

}
