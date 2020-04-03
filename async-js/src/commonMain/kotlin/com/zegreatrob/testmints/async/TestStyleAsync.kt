package com.zegreatrob.testmints.async

import kotlinx.coroutines.*

expect fun <T> testAsync(block: suspend CoroutineScope.() -> T): Any?

suspend fun <C> setupAsync(context: C, additionalSetup: suspend C.() -> Unit = {}) = SetupAsync(context).apply { additionalSetup(context) }

fun <C : Any> setupAsync2(context: C, additionalActions: suspend C.() -> Unit = {}) = Setup(
        { context },
        context.chooseTestScope(),
        additionalActions
)

fun <C : Any> setupAsync2(contextProvider: suspend () -> C, additionalActions: suspend C.() -> Unit = {}) = Setup(
        contextProvider,
        MainScope(),
        additionalActions
)

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
            if (context is ScopeMint) {
                waitForJobsToFinish(context.setupScope)
            }
            additionalActions()

            context to codeUnderTest()
        }
    }.let { Exercise(scope, it) }

}

private suspend fun waitForJobsToFinish(scope: CoroutineScope) {
    val job = scope.coroutineContext[Job]
    job?.children?.toList()?.joinAll()
    scope.cancel()
}

class Exercise<C, R>(private val scope: CoroutineScope, private val deferred: Deferred<Pair<C, R>>) {
    infix fun <R2> verify(assertionFunctions: suspend C.(R) -> R2) = scope.async {
        val (context, result) = deferred.await()
        if (context is ScopeMint) {
            waitForJobsToFinish(context.exerciseScope)
        }
        context.assertionFunctions(result)
    }.apply {
        invokeOnCompletion { cause -> scope.cancel(cause?.wrapCause()) }
    }.let { finalTransform(it) }

    private fun Throwable.wrapCause() = CancellationException("Test failure.", this)

}

expect fun <C, R, R2> Exercise<C, R>.finalTransform(it: Deferred<R2>): Any?

abstract class ScopeMint {
    val testScope = mintScope()
    val setupScope = mintScope() + CoroutineName("Setup")
    val exerciseScope = mintScope() + CoroutineName("Exercise")
}

private fun Any.chooseTestScope() = if (this is ScopeMint) {
    testScope
} else
    mintScope()

private fun mintScope() = MainScope() + CoroutineName("testMintAsync")