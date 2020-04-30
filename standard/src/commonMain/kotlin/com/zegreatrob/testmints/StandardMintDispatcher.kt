package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.MintReporter
import com.zegreatrob.testmints.report.ReporterProvider


interface StandardMintDispatcher : ReporterProvider {

// Vernacular based on http://xunitpatterns.com/Four%20Phase%20Test.html

    fun <C : Any> setup(context: C, additionalSetupActions: C.() -> Unit = {}) = Setup(context, reporter, additionalSetupActions)

    class Setup<C : Any>(
            private val context: C,
            private val reporter: MintReporter,
            private val additionalSetupActions: C.() -> Unit) {
        infix fun <R> exercise(codeUnderTest: C.() -> R) = context
                .also(additionalSetupActions)
                .also { reporter.exerciseStart(context) }
                .codeUnderTest()
                .let { Exercise(context, it, reporter) }
                .also { reporter.exerciseFinish() }
    }

    class Exercise<C, R>(private val context: C, private val result: R, private val reporter: MintReporter) {
        infix fun <R2> verify(assertionFunctions: C.(R) -> R2) = doVerify(assertionFunctions)
                .let { if (it != null) throw it else Unit }

        private fun <R2> doVerify(assertionFunctions: C.(R) -> R2) = context
                .also { reporter.verifyStart(result) }
                .let { captureException { it.assertionFunctions(result) } }
                .also { reporter.verifyFinish() }

        infix fun <R2> verifyAnd(assertionFunctions: C.(R) -> R2) =
                Verify(context, result, reporter, doVerify(assertionFunctions))
    }

    class Verify<C, R>(private val context: C, private val result: R, private val reporter: MintReporter, private val failure: Throwable?) {
        infix fun teardown(teardownFunctions: C.(R) -> Unit): Unit = context.also { reporter.teardownStart() }
                .run { captureException { teardownFunctions(result) } }
                .also { reporter.teardownFinish() }
                .let(::handleTeardownExceptions)

        private fun handleTeardownExceptions(it: Throwable?) = when {
            failure != null && it != null -> throw CompoundMintTestException(failure, it)
            failure != null -> throw failure
            it != null -> throw it
            else -> Unit
        }

    }

}
