package com.zegreatrob.react.dataloader

import kotlinx.coroutines.*
import react.*

fun useScope(coroutineName: String): CoroutineScope {
    val (scope) = useState { MainScope() + CoroutineName(coroutineName) }
    useEffectOnce { cleanup { scope.cancel() } }
    return scope
}
