package com.zegreatrob.testmints


interface StandardMintDispatcher {

    val reporter: MintReporter

// Vernacular based on http://xunitpatterns.com/Four%20Phase%20Test.html

    fun <C : Any> setup(context: C) = Setup(context, reporter)

    class Setup<C : Any>(private val context: C, private val reporter: MintReporter) {
        infix fun <R> exercise(codeUnderTest: C.() -> R) = context
                .also { reporter.exerciseStart(context) }
                .codeUnderTest()
                .let { Exercise(context, it, reporter) }
                .also { reporter.exerciseFinish() }
    }

    class Exercise<C, R>(private val context: C, private val result: R, private val reporter: MintReporter) {
        infix fun <R2> verify(assertionFunctions: C.(R) -> R2) = context
                .also { reporter.verifyStart(result) }
                .assertionFunctions(result)
                .also { reporter.verifyFinish() }

        infix fun <R2> verifyAnd(assertionFunctions: C.(R) -> R2) = context
                .also { verify(assertionFunctions) }
                .let { Verify(context, result, reporter) }
    }

    class Verify<C, R>(private val context: C, private val result: R, private val reporter: MintReporter) {
        infix fun teardown(teardownFunctions: C.(R) -> Unit) = context.also { reporter.teardownStart() }
                .run { teardownFunctions(result) }
                .also { reporter.teardownFinish() }

    }

}
