package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.MintReporter
import com.zegreatrob.testmints.report.ReporterProvider


interface StandardMintDispatcher : ReporterProvider {

// Vernacular based on http://xunitpatterns.com/Four%20Phase%20Test.html

    fun <C : Any> setup(context: C, additionalSetupActions: C.() -> Unit = {}) = Setup(context, reporter, additionalSetupActions)

    fun testTemplate(sharedSetup: () -> Unit, sharedTeardown: () -> Unit) = TestTemplate(sharedSetup, sharedTeardown)

}

class TestTemplate(private val templateSetup: () -> Unit, private val templateTeardown: () -> Unit) {
    operator fun <C : Any> invoke(context: C, additionalSetupActions: C.() -> Unit = {}) =
            Setup(context, StandardMints.reporter, additionalSetupActions, templateSetup, templateTeardown)
}

class Setup<C : Any>(
        private val context: C,
        private val reporter: MintReporter,
        private val additionalSetupActions: C.() -> Unit,
        private val templateSetup: () -> Unit = {},
        private val templateTeardown: () -> Unit = {}
) {
    infix fun <R> exercise(codeUnderTest: C.() -> R) = context
            .also { templateSetup() }
            .also(additionalSetupActions)
            .also { reporter.exerciseStart(context) }
            .let { it.codeUnderTest() }
            .let { Exercise(context, it, reporter, templateTeardown) }
            .also { reporter.exerciseFinish() }
}

class Exercise<C, R>(
        private val context: C,
        private val result: R,
        private val reporter: MintReporter,
        private val templateTeardown: () -> Unit = {}
) {
    infix fun <R2> verify(assertionFunctions: C.(R) -> R2) = doVerify(assertionFunctions)
            .let { if (it != null) throw it else Unit }

    private fun <R2> doVerify(assertionFunctions: C.(R) -> R2) = context
            .also { reporter.verifyStart(result) }
            .let { captureException { it.assertionFunctions(result) } }
            .also { reporter.verifyFinish() }

    infix fun <R2> verifyAnd(assertionFunctions: C.(R) -> R2) =
            Verify(context, result, reporter, doVerify(assertionFunctions), templateTeardown)
}

class Verify<C, R>(
        private val context: C,
        private val result: R,
        private val reporter: MintReporter,
        private val failure: Throwable?,
        private val templateTeardown: () -> Unit = {}
) {
    infix fun teardown(teardownFunctions: C.(R) -> Unit): Unit = context.also { reporter.teardownStart() }
            .run { captureException { teardownFunctions(result) } }
            .let { it to captureException { templateTeardown() } }
            .also { reporter.teardownFinish() }
            .let(::handleTeardownExceptions)

    private fun handleTeardownExceptions(pair: Pair<Throwable?, Throwable?>) {
        val (teardownException, templateTeardownException) = pair
        val problems = exceptionDescriptionMap(teardownException, templateTeardownException)

        if (problems.size == 1) {
            throw problems.values.first()
        } else if (problems.isNotEmpty()) {
            throw CompoundMintTestException(problems)
        }
    }

    private fun exceptionDescriptionMap(teardownException: Throwable?, templateTeardownException: Throwable?) =
            mapOf(
                    "Failure" to failure,
                    "Teardown exception" to teardownException,
                    "Template teardown exception" to templateTeardownException
            )
                    .mapNotNull { (descriptor, exception) -> exception?.let { descriptor to exception } }
                    .toMap()

}

