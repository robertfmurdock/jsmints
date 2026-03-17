package com.zegreatrob.react.dataloader.external.react

internal external interface EffectHook {
    operator fun invoke(
        effect: () -> (() -> Unit)?,
        dependencies: Array<Any> = definedExternally,
    )
}
