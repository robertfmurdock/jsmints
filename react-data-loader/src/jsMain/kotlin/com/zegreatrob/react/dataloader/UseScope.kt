package com.zegreatrob.react.dataloader

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus
import react.useEffectOnce
import react.useState

fun useScope(coroutineName: String): CoroutineScope {
    val (scope) = useState { MainScope() + CoroutineName(coroutineName) }
    useEffectOnce { cleanup { scope.cancel() } }
    return scope
}
