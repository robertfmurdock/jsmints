package com.zegreatrob.testmints.async

import kotlinx.coroutines.*

class Exercise<C, R>(private val scope: CoroutineScope, private val deferred: () -> Deferred<Pair<C, R>>) {
    infix fun <R2> verify(assertionFunctions: suspend C.(R) -> R2) = finalTransform {
        scope.async {
            val (context, result) = deferred().await()
            if (context is ScopeMint) {
                waitForJobsToFinish(context.exerciseScope)
            }
            context.assertionFunctions(result)
        }.apply {
            invokeOnCompletion { cause -> scope.cancel(cause?.wrapCause()) }
        }
    }

    private fun Throwable.wrapCause() = CancellationException("Test failure.", this)
}
