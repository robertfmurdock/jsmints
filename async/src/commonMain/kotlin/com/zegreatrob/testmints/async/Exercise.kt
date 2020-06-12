package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.CompoundMintTestException
import com.zegreatrob.testmints.captureException
import com.zegreatrob.testmints.report.MintReporter
import kotlinx.coroutines.*

class Exercise<C : Any, R>(
        private val scope: CoroutineScope,
        private val reporter: MintReporter,
        private val contextProvider: suspend () -> C,
        private val additionalSetupActions: suspend C.() -> Unit,
        private val exerciseFunc: suspend C.() -> R,
        private val templateSetup: suspend () -> Unit = {},
        private val templateTeardown: suspend () -> Unit = {}
) {

    private val contextDeferred = scope.async(start = CoroutineStart.LAZY) { templateSetup();contextProvider() }

    private val exerciseDeferred = scope.async(start = CoroutineStart.LAZY) {
        val context = contextDeferred.await()
        with(context) {
            additionalSetupActions()
            if (context is ScopeMint) {
                waitForJobsToFinish(context.setupScope)
            }

            runCodeUnderTest(context, exerciseFunc)
        }
    }

    private suspend fun <R> runCodeUnderTest(context: C, codeUnderTest: suspend C.() -> R): R {
        reporter.exerciseStart(context)
        val result = codeUnderTest(context)
        reporter.exerciseFinish()
        return result
    }

    infix fun <R2> verify(assertionFunctions: suspend C.(R) -> R2) = finalTransform {
        doVerifyAsync(assertionFunctions).apply {
            invokeOnCompletion { cause -> scope.cancel(cause?.wrapCause()) }
        }
    }

    private fun <R2> doVerifyAsync(assertionFunctions: suspend C.(R) -> R2) = scope.async {
        val context = contextDeferred.await()
        val result = exerciseDeferred.await()
        if (context is ScopeMint) {
            waitForJobsToFinish(context.exerciseScope)
        }
        reporter.verifyStart(result)
        context.assertionFunctions(result)
        reporter.verifyFinish()
    }

    infix fun <R2> verifyAnd(assertionFunctions: suspend C.(R) -> R2): Verify<C, R> {
        val verifyDeferred = doVerifyAsync(assertionFunctions)
        return Verify(reporter, verifyDeferred, scope, contextDeferred, exerciseDeferred, templateTeardown)
    }

}

private fun Throwable.wrapCause() = CancellationException("Test failure.", this)

class Verify<C, R>(
        private val reporter: MintReporter,
        private val deferred: Deferred<Unit>,
        private val scope: CoroutineScope,
        private val contextDeferred: Deferred<C>,
        private val exerciseDeferred: Deferred<R>,
        private val templateTeardown: suspend () -> Unit
) {

    infix fun teardown(function: suspend C.(R) -> Unit) = finalTransform {
        teardownAsync(function).apply {
            invokeOnCompletion { cause -> scope.cancel(cause?.wrapCause()) }
        }
    }

    private fun teardownAsync(teardownFunction: suspend C.(R) -> Unit) = scope.async {
        val context = contextDeferred.await()
        val result = exerciseDeferred.await()
        val failure = captureException { deferred.await() }
        reporter.teardownStart()
        val teardownException = try {
            teardownFunction(context, result)
            null
        } catch (exception: Throwable) {
            exception
        }

        val templateTeardownException = catchingTemplateTeardown()
        reporter.teardownFinish()
        handleTeardownExceptions(failure, teardownException, templateTeardownException)
    }

    private suspend fun catchingTemplateTeardown() = captureException { templateTeardown() }

    private fun handleTeardownExceptions(
            failure: Throwable?,
            teardownException: Throwable?,
            templateTeardownException: Throwable?
    ) {
        val problems = exceptionDescriptionMap(failure, teardownException, templateTeardownException)

        if (problems.size == 1) {
            throw problems.values.first()
        } else if (problems.isNotEmpty()) {
            throw CompoundMintTestException(problems)
        }
    }

    private fun exceptionDescriptionMap(
            failure: Throwable?,
            teardownException: Throwable?,
            templateTeardownException: Throwable?
    ) = descriptionMap(failure, teardownException, templateTeardownException)
            .mapNotNull { (descriptor, exception) -> exception?.let { descriptor to exception } }
            .toMap()

    private fun descriptionMap(
            failure: Throwable?,
            teardownException: Throwable?,
            templateTeardownException: Throwable?
    ) = mapOf(
            "Failure" to failure,
            "Teardown exception" to teardownException,
            "Template teardown exception" to templateTeardownException
    )
}
