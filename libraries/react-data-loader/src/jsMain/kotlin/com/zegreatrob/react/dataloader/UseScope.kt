package com.zegreatrob.react.dataloader

import kotlinx.coroutines.*
import react.useEffectOnce
import react.useState

fun useScope(coroutineName: String): CoroutineScope {
    val scope = useState { MainScope() + CoroutineName(coroutineName) }.component1()
    useEffectOnce {
        this.coroutineContext.job.invokeOnCompletion {
            scope.cancel()
        }
    }
    return scope
}
