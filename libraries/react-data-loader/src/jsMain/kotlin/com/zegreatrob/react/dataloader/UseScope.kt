@file:OptIn(ExperimentalWasmJsInterop::class)

package com.zegreatrob.react.dataloader

import com.zegreatrob.react.dataloader.react.useEffect
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus
import react.useState

fun useScope(coroutineName: String): CoroutineScope {
    val scope = useState { MainScope() + CoroutineName(coroutineName) }.component1()
    useEffect({
        { scope.cancel() }
    }, dependencies = arrayOf())
    return scope
}
