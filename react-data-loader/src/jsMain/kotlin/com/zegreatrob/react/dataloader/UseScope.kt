package com.zegreatrob.react.dataloader

import kotlinx.coroutines.*
import react.useEffect
import react.useState

fun useScope(coroutineName: String): CoroutineScope {
    val (scope) = useState { MainScope() + CoroutineName(coroutineName) }
    useEffect { cleanup { scope.cancel() } }
    return scope
}
