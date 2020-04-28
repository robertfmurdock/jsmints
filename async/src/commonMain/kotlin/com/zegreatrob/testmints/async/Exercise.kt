package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.report.MintReporter
import kotlinx.coroutines.*

class Exercise<C, R>(
        private val scope: CoroutineScope,
        private val reporter: MintReporter,
        private val deferred: () -> Deferred<Pair<C, R>>
) {
    infix fun <R2> verify(assertionFunctions: suspend C.(R) -> R2) = finalTransform {
        verifyAsync(assertionFunctions).apply {
            invokeOnCompletion { cause -> scope.cancel(cause?.wrapCause()) }
        }
    }

    private fun <R2> verifyAsync(assertionFunctions: suspend C.(R) -> R2) = scope.async {
        val (context, result) = deferred().await()
        if (context is ScopeMint) {
            waitForJobsToFinish(context.exerciseScope)
        }
        reporter.verifyStart(result)
        context.assertionFunctions(result)
        reporter.verifyFinish()
    }

    infix fun <R2> verifyAnd(assertionFunctions: suspend C.(R) -> R2): Verify {
        val deferred = verifyAsync(assertionFunctions)
        return Verify(reporter, deferred, scope)
    }

}

private fun Throwable.wrapCause() = CancellationException("Test failure.", this)

class Verify(private val reporter: MintReporter, private val deferred: Deferred<Unit>, private val scope: CoroutineScope) {
    infix fun teardown(function: suspend () -> Unit) = finalTransform {
        teardownAsync(function).apply {
            invokeOnCompletion { cause -> scope.cancel(cause?.wrapCause()) }
        }
    }

    private fun teardownAsync(function: suspend () -> Unit) = scope.async {
        deferred.await()
        reporter.teardownStart()
        function()
        reporter.teardownFinish()
    }


}
