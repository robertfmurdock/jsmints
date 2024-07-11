package com.zegreatrob.react.dataloader

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus
import react.useEffectOnceWithCleanup
import react.useState

fun useScope(coroutineName: String): CoroutineScope {
    val (scope) = useState { MainScope() + CoroutineName(coroutineName) }
    useEffectOnceWithCleanup { onCleanup { scope.cancel() } }
    return scope
}
