package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.report.MintReporter
import kotlinx.coroutines.CoroutineScope

class Setup<C : Any, SC : Any>(
        private val contextProvider: suspend (SC) -> C,
        private val scope: CoroutineScope,
        private val additionalActions: suspend C.() -> Unit,
        private val reporter: MintReporter,
        private val templateSetup: suspend () -> SC,
        private val templateTeardown: suspend (SC) -> Unit = {}
) {
    infix fun <R> exercise(codeUnderTest: suspend C.() -> R) = Exercise(
            scope,
            reporter,
            contextProvider,
            additionalActions,
            codeUnderTest,
            templateSetup,
            templateTeardown
    )

}
