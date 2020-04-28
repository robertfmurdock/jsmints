package com.zegreatrob.testmints.async

class ExerciseAsync<C, R>(private val context: C, val result: R) {
    suspend infix fun <R2> verifyAsync(assertionFunctions: suspend C.(R) -> R2) =
            context.assertionFunctions(result)
}