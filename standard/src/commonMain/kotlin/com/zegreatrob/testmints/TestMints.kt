package com.zegreatrob.testmints

// Vernacular based on http://xunitpatterns.com/Four%20Phase%20Test.html

fun <C> setup(context: C) = Setup(context)

class Setup<C>(private val context: C) {
    infix fun <R> exercise(codeUnderTest: C.() -> R) = context.codeUnderTest()
            .let { Exercise(context, it) }
}

class Exercise<C, R>(private val context: C, val result: R) {
    infix fun <R2> verify(assertionFunctions: C.(R) -> R2) = context.assertionFunctions(result)
}
