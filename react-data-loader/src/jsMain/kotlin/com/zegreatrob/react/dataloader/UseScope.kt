package com.zegreatrob.react.dataloader

import kotlinx.coroutines.*
import react.RCleanup
import react.useEffectWithCleanup
import react.useState

fun useScope(coroutineName: String): CoroutineScope {
    val (scope) = useState { MainScope() + CoroutineName(coroutineName) }
    val cleanup: RCleanup = { scope.cancel() }
    useEffectWithCleanup(dependencies = emptyList()) { cleanup }
    return scope
}
