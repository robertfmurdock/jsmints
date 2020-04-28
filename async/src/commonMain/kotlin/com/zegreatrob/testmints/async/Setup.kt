package com.zegreatrob.testmints.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

fun <C : Any> setupAsync2(context: C, additionalActions: suspend C.() -> Unit = {}) = Setup(
        { context },
        context.chooseTestScope(),
        additionalActions
)

fun <C : Any> setupAsync2(contextProvider: suspend () -> C, additionalActions: suspend C.() -> Unit = {}) = Setup(
        contextProvider,
        mintScope(),
        additionalActions
)

private fun Any.chooseTestScope() = if (this is ScopeMint) testScope else mintScope()

class Setup<C>(
        private val contextProvider: suspend () -> C,
        private val scope: CoroutineScope,
        private val additionalActions: suspend C.() -> Unit
) {
    infix fun <R> exercise(codeUnderTest: suspend C.() -> R) = Exercise(scope) {
        scope.async {
            val context = contextProvider()
            with(context) {
                if (context is ScopeMint) {
                    waitForJobsToFinish(context.setupScope)
                }
                additionalActions()

                context to codeUnderTest()
            }
        }
    }

}
