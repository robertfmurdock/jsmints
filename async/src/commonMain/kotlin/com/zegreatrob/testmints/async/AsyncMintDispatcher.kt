package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.report.MintReporter

interface AsyncMintDispatcher : SetupSyntax

interface SetupSyntax : ReporterProvider {
    fun <C : Any> setupAsync2(context: C, additionalActions: suspend C.() -> Unit = {}) = Setup(
            { context },
            context.chooseTestScope(),
            additionalActions,
            reporter
    )

    fun <C : Any> setupAsync2(contextProvider: suspend () -> C, additionalActions: suspend C.() -> Unit = {}) = Setup(
            contextProvider,
            mintScope(),
            additionalActions,
            reporter
    )

    private fun Any.chooseTestScope() = if (this is ScopeMint) testScope else mintScope()

}

fun <C : Any> setupAsync2(context: C, additionalActions: suspend C.() -> Unit = {}) = AsyncMints
        .setupAsync2(context, additionalActions)

fun <C : Any> setupAsync2(contextProvider: suspend () -> C, additionalActions: suspend C.() -> Unit = {}) = AsyncMints
        .setupAsync2(contextProvider, additionalActions)

interface ReporterProvider {
    val reporter: MintReporter
}

object AsyncMints : AsyncMintDispatcher {
    override var reporter: MintReporter = object : MintReporter {}
}