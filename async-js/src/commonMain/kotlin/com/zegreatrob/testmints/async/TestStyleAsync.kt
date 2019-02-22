package com.zegreatrob.testmints.async

import kotlinx.coroutines.CoroutineScope

expect fun <T> testAsync(block: suspend CoroutineScope.() -> T): Any?

suspend fun <C> setupAsync(context: C, additionalSetup: suspend C.() -> Unit = {}) = SetupAsync(context).apply { additionalSetup(context) }

class SetupAsync<C>(private val context: C) {
    suspend infix fun <R> exerciseAsync(codeUnderTest: suspend C.() -> R) =
            context.codeUnderTest()
                    .let { ExerciseAsync(context, it) }
}

class ExerciseAsync<C, R>(private val context: C, val result: R) {
    suspend infix fun <R2> verifyAsync(assertionFunctions: suspend C.(R) -> R2) =
            context.assertionFunctions(result)
}
