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
                runTestAsync(assertionFunctions, teardownFunc)
            }

    private fun <R2> runTestAsync(assertionFunctions: suspend C.(R) -> R2, teardownFunc: suspend C.(R) -> Unit) =
            scope.async { runTest(assertionFunctions, teardownFunc) }.apply {
                invokeOnCompletion { cause -> scope.cancel(cause?.wrapCause()) }
            }

    private suspend fun <R2> runTest(assertionFunctions: suspend C.(R) -> R2, teardownFunc: suspend C.(R) -> Unit) {
        val (sharedContext, context) = performSetup()
        val result = performExercise(context)
        val failure = performVerify(context, result, assertionFunctions)
        performTeardown(sharedContext, context, result, failure, teardownFunc)
    }

    private suspend fun performTeardown(
            sharedContext: SC,
            context: C,
            result: R,
            failure: Throwable?,
            teardownFunc: suspend C.(R) -> Unit
    ) {
        reporter.teardownStart()
        val teardownException = try {
            teardownFunc(context, result)
            null
        } catch (exception: Throwable) {
            exception
        }

        val templateTeardownException = performTemplateTeardown(sharedContext)
        reporter.teardownFinish()
        handleTeardownExceptions(failure, teardownException, templateTeardownException)
    }

    private suspend fun <R2> performVerify(context: C, result: R, assertionFunctions: suspend C.(R) -> R2) =
            captureException {
                reporter.verifyStart(result)
                context.assertionFunctions(result)
                reporter.verifyFinish()
            }

    private suspend fun performSetup(): Pair<SC, C> {
        val sharedContext = templateSetup()
        val context = contextProvider(sharedContext)
        additionalSetupActions(context)
        if (context is ScopeMint) {
            waitForJobsToFinish(context.setupScope)
        }
        return Pair(sharedContext, context)
    }

    private suspend fun performExercise(context: C) = runCodeUnderTest(context, exerciseFunc)
            .also {
                if (context is ScopeMint) {
                    waitForJobsToFinish(context.exerciseScope)
                }
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