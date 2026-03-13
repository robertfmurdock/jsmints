@file:OptIn(ExperimentalWasmJsInterop::class)

package com.zegreatrob.react.dataloader.react

external interface EffectHook {
    operator fun invoke(
        effect: () -> () -> Unit?,
        dependencies: JsArray<Any?> = definedExternally,
    )
}
