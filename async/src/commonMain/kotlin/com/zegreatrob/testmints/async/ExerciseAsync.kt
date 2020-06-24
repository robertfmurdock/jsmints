package com.zegreatrob.testmints.async

class ExerciseAsync<C, R>(private val context: C, private val result: R) {
    @Suppress("unused")
    suspend infix fun <R2> verifyAsync(assertionFunctions: suspend C.(R) -> R2) =
            context.assertionFunctions(result)
}