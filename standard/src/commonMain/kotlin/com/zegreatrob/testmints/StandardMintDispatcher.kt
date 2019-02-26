package com.zegreatrob.testmints


interface StandardMintDispatcher {

    val reporter: MintReporter

// Vernacular based on http://xunitpatterns.com/Four%20Phase%20Test.html

    fun <C> setup(context: C) = Setup(context, reporter)

    class Setup<C>(private val context: C, private val reporter: MintReporter) {
        infix fun <R> exercise(codeUnderTest: C.() -> R) = context
                .also { reporter.exerciseStart() }
                .codeUnderTest()
                .let { Exercise(context, it, reporter) }
                .also { reporter.exerciseFinish() }
    }

    class Exercise<C, R>(private val context: C, private val result: R, private val reporter: MintReporter) {
        infix fun <R2> verify(assertionFunctions: C.(R) -> R2) = context
                .also { reporter.verifyStart() }
                .assertionFunctions(result)
                .also { reporter.verifyFinish() }
    }

}