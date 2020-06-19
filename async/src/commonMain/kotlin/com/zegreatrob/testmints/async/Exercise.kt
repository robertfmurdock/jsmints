package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.CompoundMintTestException
import com.zegreatrob.testmints.captureException
import com.zegreatrob.testmints.report.MintReporter
import kotlinx.coroutines.*

class Exercise<C : Any, R, SC : Any>(
        private val scope: CoroutineScope,
        private val reporter: MintReporter,
        private val contextProvider: suspend (SC) -> C,
        private val additionalSetupActions: suspend C.() -> Unit,
        private val exerciseFunc: suspend C.() -> R,
        private val templateSetup: suspend () -> SC,
        private val templateTeardown: suspend (SC) -> Unit = {}
) {

    private val sharedContextDeferred = scope.async(start = CoroutineStart.LAZY) { templateSetup() }

    private val contextDeferred = scope.async(start = CoroutineStart.LAZY) {
        contextProvider(sharedContextDeferred.await())
    }

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

    infix fun verify(assertionFunctions: suspend C.(R) -> Unit) = finalTransform {
        runTestAsync { }(assertionFunctions)
    }

    infix fun <R2> verifyAnd(assertionFunctions: suspend C.(R) -> R2) = Verify(runTestAsync(assertionFunctions))

    private fun <R2> runTestAsync(assertionFunctions: suspend C.(R) -> R2): (suspend C.(R) -> Unit) -> Deferred<Unit> =
            { teardownFunc ->
                val verifyDeferred = doVerifyAsync(assertionFunctions)
                runTestAsync(teardownFunc, verifyDeferred)
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

    private fun runTestAsync(function: suspend C.(R) -> Unit, verifyDeferred: Deferred<Unit>) = teardownAsync(function, verifyDeferred).apply {
        invokeOnCompletion { cause -> scope.cancel(cause?.wrapCause()) }
    }

    private fun teardownAsync(teardownFunction: suspend C.(R) -> Unit, verifyDeferred: Deferred<Unit>) = scope.async {
        val sharedContext = sharedContextDeferred.await()
        val context = contextDeferred.await()
        val result = exerciseDeferred.await()
        val failure = captureException { verifyDeferred.await() }
        reporter.teardownStart()
        val teardownException = try {
            teardownFunction(context, result)
            null
        } catch (exception: Throwable) {
            exception
        }

        val templateTeardownException = performTemplateTeardown(sharedContext)
        reporter.teardownFinish()
        handleTeardownExceptions(failure, teardownException, templateTeardownException)
    }

    private suspend fun performTemplateTeardown(sharedContext: SC) = captureException {
        templateTeardown(sharedContext)
    }
}

private fun Throwable.wrapCause() = CancellationException("Test failure.", this)

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