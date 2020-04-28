package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.report.MintReporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

class Setup<C : Any>(
        private val contextProvider: suspend () -> C,
        private val scope: CoroutineScope,
        private val additionalActions: suspend C.() -> Unit,
        private val reporter: MintReporter
) {
    infix fun <R> exercise(codeUnderTest: suspend C.() -> R) = Exercise(scope, reporter) {
        scope.async {
            val context = contextProvider()
            with(context) {
                if (context is ScopeMint) {
                    waitForJobsToFinish(context.setupScope)
                }
                additionalActions()

                val result = runCodeUnderTest(context, codeUnderTest)
                context to result
            }
        }
    }

    private suspend fun <R> runCodeUnderTest(context: C, codeUnderTest: suspend C.() -> R): R {
        reporter.exerciseStart(context)
        val result = codeUnderTest(context)
        reporter.exerciseFinish()
        return result
    }
}
