package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.MintReporter
import com.zegreatrob.testmints.report.ReporterProvider


interface StandardMintDispatcher : ReporterProvider {

// Vernacular based on http://xunitpatterns.com/Four%20Phase%20Test.html

    fun <C : Any> setup(context: C, additionalSetupActions: C.() -> Unit = {}) = Setup(
            context, reporter, additionalSetupActions, {}
    )

    fun <SC : Any> testTemplate(sharedSetup: () -> SC, sharedTeardown: (SC) -> Unit) = TestTemplate(
            sharedSetup, sharedTeardown, reporter
    )

    fun testTemplate(wrapper: (() -> Unit) -> Unit): TestTemplate<Unit> = TestTemplate(
            { }, { }, reporter, wrapper
    )

}

class TestTemplate<SC : Any>(
        private val templateSetup: () -> SC,
        private val templateTeardown: (SC) -> Unit,
        private val reporter: MintReporter,
        private val wrapper: (() -> Unit) -> Unit = { it() }
) {
    operator fun <C : Any> invoke(context: C, additionalSetupActions: C.() -> Unit = {}) =
            Setup(context, reporter, additionalSetupActions, templateSetup, templateTeardown, wrapper)

    fun extend(sharedSetup: () -> Unit = {}, sharedTeardown: () -> Unit = {}) = TestTemplate(
            templateSetup = { templateSetup().also { sharedSetup() } },
            templateTeardown = { sharedTeardown(); templateTeardown(it) },
            reporter = reporter,
            wrapper = wrapper
    )
}

typealias ExerciseFunc<C, R> = C.() -> R
typealias VerifyFunc<C, R> = C.(R) -> Any
typealias TeardownFunc<C, R> = C.(R) -> Unit

class Setup<C : Any, SC : Any>(
        private val context: C,
        private val reporter: MintReporter,
        private val additionalSetupActions: C.() -> Unit,
        private val templateSetup: () -> SC,
        private val templateTeardown: (SC) -> Unit = {},
        private val wrapper: (() -> Unit) -> Unit = { it() }
) {
    infix fun <R> exercise(codeUnderTest: C.() -> R) = Exercise<C, R> { assertionFunctions ->
        { teardownFunctions ->
            runTest(codeUnderTest, assertionFunctions, teardownFunctions)
        }
    }

    private fun <R> runTest(
            exerciseFunc: ExerciseFunc<C, R>,
            verifyFunc: VerifyFunc<C, R>,
            teardownFunc: TeardownFunc<C, R>
    ) = checkedInvoke(wrapper) {
        val reportedExerciseFunc = exerciseFunc.makeReporting(reporter)
        val reportedVerifyFunc = verifyFunc.makeReporting(reporter)
        val reportedTeardownFunc = teardownFunc.plus(templateTeardown).makeReporting(reporter)

        val sharedContext = setup()
        val result = reportedExerciseFunc(context)
        val failure = reportedVerifyFunc(context, result)

        reportedTeardownFunc(sharedContext, context, result, failure)
    }

    private fun setup(): SC {
        val sharedContext = templateSetup()
        additionalSetupActions(context)
        return sharedContext
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

private fun <SC : Any, C : Any, R> TeardownFunc<C, R>.plus(templateTeardown: (SC) -> Unit) =
        { sc: SC, c: C, r: R -> captureException { c.(this)(r) } to captureException { templateTeardown(sc) } }

private fun <SC : Any, C : Any, R> ((SC, C, R) -> Pair<Throwable?, Throwable?>).makeReporting(mintReporter: MintReporter) = { sharedContext: SC, context: C, result: R, failure: Throwable? ->
    context.also { mintReporter.teardownStart() }
            .run { this@makeReporting(sharedContext, context, result) }
            .also { mintReporter.teardownFinish() }
            .let { handleTeardownExceptions(it, failure) }
}

private fun handleTeardownExceptions(pair: Pair<Throwable?, Throwable?>, failure: Throwable?) {
    val (teardownException, templateTeardownException) = pair
    val problems = exceptionDescriptionMap(teardownException, templateTeardownException, failure)

    if (problems.size == 1) {
        throw problems.values.first()
    } else if (problems.isNotEmpty()) {
        throw CompoundMintTestException(problems)
    }
}

private fun exceptionDescriptionMap(teardownException: Throwable?, templateTeardownException: Throwable?, failure: Throwable?) =
        mapOf(
                "Failure" to failure,
                "Teardown exception" to teardownException,
                "Template teardown exception" to templateTeardownException
        )
                .mapNotNull { (descriptor, exception) -> exception?.let { descriptor to exception } }
                .toMap()

private fun checkedInvoke(wrapper: (() -> Unit) -> Unit, test: () -> Unit) {
    var testWasInvoked = false
    wrapper.invoke {
        testWasInvoked = true
        test()
    }
    if (!testWasInvoked) throw Exception("Incomplete test template: the wrapper function never called the test function")
}
