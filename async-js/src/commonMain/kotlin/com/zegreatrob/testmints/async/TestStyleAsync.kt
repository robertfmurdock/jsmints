package com.zegreatrob.testmints.async

import kotlinx.coroutines.*

expect fun <T> testAsync(block: suspend CoroutineScope.() -> T): Any?

suspend fun <C> setupAsync(context: C, additionalSetup: suspend C.() -> Unit = {}) = SetupAsync(context).apply { additionalSetup(context) }

expect fun <C : Any> setupAsync2(context: C, additionalActions: suspend C.() -> Unit = {}): Setup<C>

class SetupAsync<C>(private val context: C) {
    suspend infix fun <R> exerciseAsync(codeUnderTest: suspend C.() -> R) =
            context.codeUnderTest()
                    .let { ExerciseAsync(context, it) }
}

class ExerciseAsync<C, R>(private val context: C, val result: R) {
    suspend infix fun <R2> verifyAsync(assertionFunctions: suspend C.(R) -> R2) =
            context.assertionFunctions(result)
}

class Setup<C>(
        private val contextProvider: suspend () -> C,
        private val scope: CoroutineScope,
        private val additionalActions: suspend C.() -> Unit
) {
    infix fun <R> exercise(codeUnderTest: suspend C.() -> R) = scope.async {
        val context = contextProvider()
        with(context) {
            additionalActions()
            context to codeUnderTest()
        }
    }.let { Exercise(scope, it) }
}

class Exercise<C, R>(private val scope: CoroutineScope, private val deferred: Deferred<Pair<C, R>>) {
    infix fun <R2> verify(assertionFunctions: suspend C.(R) -> R2) = scope.async {
        val (context, result) = deferred.await()
        context.assertionFunctions(result)
    }.apply {
        invokeOnCompletion { cause -> scope.cancel(cause?.let { CancellationException("Test failure.", cause) }) }
    }.let { finalTransform(it) }

}

expect fun <C, R, R2> Exercise<C, R>.finalTransform(it: Deferred<R2>): Any?
