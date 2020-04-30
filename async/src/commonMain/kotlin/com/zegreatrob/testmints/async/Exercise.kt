package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.CompoundMintTestException
import com.zegreatrob.testmints.captureException
import com.zegreatrob.testmints.report.MintReporter
import kotlinx.coroutines.*

class Exercise<C, R>(
        private val scope: CoroutineScope,
        private val reporter: MintReporter,
        private val deferred: () -> Deferred<Pair<C, R>>
) {
    infix fun <R2> verify(assertionFunctions: suspend C.(R) -> R2) = finalTransform {
        doVerifyAsync(assertionFunctions).apply {
            invokeOnCompletion { cause -> scope.cancel(cause?.wrapCause()) }
        }
    }

    private fun <R2> doVerifyAsync(assertionFunctions: suspend C.(R) -> R2) = scope.async {
        val (context, result) = deferred().await()
        if (context is ScopeMint) {
            waitForJobsToFinish(context.exerciseScope)
        }
        reporter.verifyStart(result)
        context.assertionFunctions(result)
        reporter.verifyFinish()
    }

    infix fun <R2> verifyAnd(assertionFunctions: suspend C.(R) -> R2): Verify {
        val deferred = doVerifyAsync(assertionFunctions)
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

    private fun teardownAsync(teardownFunction: suspend () -> Unit) = scope.async {
        val failure = captureException { deferred.await() }
        reporter.teardownStart()
        val teardownException = try {
            teardownFunction()
            null
        } catch (exception: Throwable) {
            exception
        }
        reporter.teardownFinish()
        handleTeardownExceptions(failure, teardownException)
    }

    private fun handleTeardownExceptions(failure: Throwable?, teardownException: Throwable?) = when {
        failure != null && teardownException != null -> throw CompoundMintTestException(failure, teardownException)
        failure != null -> throw failure
        teardownException != null -> throw teardownException
        else -> Unit
    }

}
