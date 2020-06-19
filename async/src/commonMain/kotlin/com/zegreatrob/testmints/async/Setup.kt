package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.CompoundMintTestException
import com.zegreatrob.testmints.captureException
import com.zegreatrob.testmints.report.MintReporter
import kotlinx.coroutines.*

class Setup<C : Any, SC : Any>(
        private val contextProvider: suspend (SC) -> C,
        private val scope: CoroutineScope,
        private val additionalActions: suspend C.() -> Unit,
        private val reporter: MintReporter,
        private val templateSetup: suspend () -> SC,
        private val templateTeardown: suspend (SC) -> Unit = {}
) {
    infix fun <R> exercise(exerciseFunc: suspend C.() -> R) = Exercise<C, R> { assertionFunctions ->
        runTestAsync(exerciseFunc, assertionFunctions)
    }

    private fun <R> runTestAsync(exerciseFunc: suspend C.() -> R, assertionFunctions: suspend C.(R) -> Unit): (suspend C.(R) -> Unit) -> Deferred<Unit> =
            { teardownFunc ->
                runTestAsync(exerciseFunc, assertionFunctions, teardownFunc)
            }

    private suspend fun <R> runCodeUnderTest(context: C, codeUnderTest: suspend C.() -> R): R {
        reporter.exerciseStart(context)
        val result = codeUnderTest(context)
        reporter.exerciseFinish()
        return result
    }

    private fun <R> runTestAsync(exerciseFunc: suspend C.() -> R, assertionFunctions: suspend C.(R) -> Unit, teardownFunc: suspend C.(R) -> Unit) =
            scope.async { runTest(exerciseFunc, assertionFunctions, teardownFunc) }.apply {
                invokeOnCompletion { cause -> scope.cancel(cause?.wrapCause()) }
            }

    private suspend fun <R> runTest(exerciseFunc: suspend C.() -> R, assertionFunctions: suspend C.(R) -> Unit, teardownFunc: suspend C.(R) -> Unit) {
        val (sharedContext, context) = performSetup()
        val result = performExercise(context, exerciseFunc)
        val failure = performVerify(context, result, assertionFunctions)
        performTeardown(sharedContext, context, result, failure, teardownFunc)
    }

    private suspend fun <R> performTeardown(
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

    private suspend fun <R> performVerify(context: C, result: R, assertionFunctions: suspend C.(R) -> Unit) =
            captureException {
                reporter.verifyStart(result)
                context.assertionFunctions(result)
                reporter.verifyFinish()
            }

    private suspend fun performSetup(): Pair<SC, C> {
        val sharedContext = templateSetup()
        val context = contextProvider(sharedContext)
        additionalActions(context)
        if (context is ScopeMint) {
            waitForJobsToFinish(context.setupScope)
        }
        return Pair(sharedContext, context)
    }

    private suspend fun <R> performExercise(context: C, exerciseFunc: suspend C.() -> R) = runCodeUnderTest(context, exerciseFunc)
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