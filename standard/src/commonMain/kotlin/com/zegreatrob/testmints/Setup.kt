package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.MintReporter

class Setup<C : Any, SC : Any>(
    private val contextProvider: (SC) -> C,
    private val reporter: MintReporter,
    private val additionalSetupActions: C.() -> Unit,
    private val wrapper: ((SC) -> Unit) -> Unit
) {
    infix fun <R> exercise(codeUnderTest: C.() -> R) = Exercise<C, R> { verifyFunc ->
        { teardownFunc ->
            runTest(codeUnderTest, verifyFunc, teardownFunc)
        }
    }

    private fun <R> runTest(
        exerciseFunc: ExerciseFunc<C, R>,
        verifyFunc: VerifyFunc<C, R>,
        teardownFunc: TeardownFunc<C, R>
    ) {
        var verifyFailure: Throwable? = null
        var teardownException: Throwable? = null
        val wrapperException = checkedInvoke(wrapper) { sharedContext ->
            val reportedExerciseFunc = exerciseFunc.makeReporting(reporter)
            val reportedVerifyFunc = verifyFunc.makeReporting(reporter)
            val reportedTeardownFunc = teardownFunc.makeReporting(reporter)

            val context = setup(sharedContext)

            val result = reportedExerciseFunc(context)
            verifyFailure = reportedVerifyFunc(context, result)
            teardownException = reportedTeardownFunc(context, result)
        }

        reporter.teardownFinish()

        reportExceptions(verifyFailure, teardownException, wrapperException)
    }

    private fun reportExceptions(
        verifyFailure: Throwable?,
        teardownException: Throwable?,
        wrapperException: Throwable?
    ) {
        val problems = exceptionDescriptionMap(teardownException, wrapperException, verifyFailure)

        if (problems.size == 1) {
            throw problems.values.first()
        } else if (problems.isNotEmpty()) {
            throw CompoundMintTestException(problems)
        }
    }

    private fun <R> TeardownFunc<C, R>.makeReporting(mintReporter: MintReporter) = { c: C, r: R ->
        mintReporter.teardownStart()
        captureException { this(c, r) }
    }

    private fun setup(sc: SC): C {
        val context = contextProvider(sc)
        additionalSetupActions(context)
        return context
    }

}

private fun <C : Any, R> ExerciseFunc<C, R>.makeReporting(reporter: MintReporter): ExerciseFunc<C, R> = {
    reporter.exerciseStart(this)
    this@makeReporting(this)
        .also { reporter.exerciseFinish() }
}

private fun <C : Any, R> VerifyFunc<C, R>.makeReporting(mintReporter: MintReporter) = { context: C, result: R ->
    context
        .also { mintReporter.verifyStart(result) }
        .let { captureException { it.(this)(result) } }
        .also { mintReporter.verifyFinish() }
}

private fun exceptionDescriptionMap(
    teardownException: Throwable?,
    templateTeardownException: Throwable?,
    failure: Throwable?
) =
    mapOf(
        "Failure" to failure,
        "Teardown exception" to teardownException,
        "Template teardown exception" to templateTeardownException
    )
        .mapNotNull { (descriptor, exception) -> exception?.let { descriptor to exception } }
        .toMap()

private fun <SC : Any> checkedInvoke(wrapper: ((SC) -> Unit) -> Unit, test: (SC) -> Unit) = captureException {
    var testWasInvoked = false
    wrapper.invoke {
        testWasInvoked = true
        test(it)
    }
    if (!testWasInvoked) throw Exception("Incomplete test template: the wrapper function never called the test function")
}
