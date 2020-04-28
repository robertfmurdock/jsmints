package com.zegreatrob.testmints.async

class SetupAsync<C>(private val context: C) {
    suspend infix fun <R> exerciseAsync(codeUnderTest: suspend C.() -> R) =
            ExerciseAsync(context, context.codeUnderTest())
}
