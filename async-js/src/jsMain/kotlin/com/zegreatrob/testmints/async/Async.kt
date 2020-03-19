package com.zegreatrob.testmints.async

import kotlinx.coroutines.*

actual fun <T> testAsync(block: suspend CoroutineScope.() -> T): Any? = GlobalScope.promise(block = block)


fun <C : Any> setupAsync2(context: C, additionalActions: suspend C.() -> Unit = {}) = Setup(
        { context },
        MainScope(),
        additionalActions
)

fun <C : Any> setupAsync2(contextProvider: suspend () -> C, additionalActions: suspend C.() -> Unit = {}) = Setup(
        contextProvider,
        MainScope(),
        additionalActions
)

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

class Exercise<C, R>(private val scope: CoroutineScope, private val deferred: Deferred<Pair<C,R>>) {
    infix fun <R2> verify(assertionFunctions: suspend C.(R) -> R2) = scope.async {
        val (context, result) = deferred.await()
        context.assertionFunctions(result)
    }.apply {
        invokeOnCompletion { cause -> scope.cancel(cause?.let { CancellationException("Test failure.", cause) }) }
    }.asPromise()

}
