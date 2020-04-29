package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.report.MintReporterConfig
import com.zegreatrob.testmints.report.ReporterProvider

interface AsyncMintDispatcher : SetupSyntax

interface SetupSyntax : ReporterProvider {
    fun <C : Any> asyncSetup(context: C, additionalActions: suspend C.() -> Unit = {}) = Setup(
            { context },
            context.chooseTestScope(),
            additionalActions,
            reporter
    )

    fun <C : Any> asyncSetup(contextProvider: suspend () -> C, additionalActions: suspend C.() -> Unit = {}) = Setup(
            contextProvider,
            mintScope(),
            additionalActions,
            reporter
    )

    private fun Any.chooseTestScope() = if (this is ScopeMint) testScope else mintScope()

}

fun <C : Any> asyncSetup(context: C, additionalActions: suspend C.() -> Unit = {}) = AsyncMints
        .asyncSetup(context, additionalActions)

fun <C : Any> asyncSetup(contextProvider: suspend () -> C, additionalActions: suspend C.() -> Unit = {}) = AsyncMints
        .asyncSetup(contextProvider, additionalActions)

@Deprecated("Ready to promote this use case to normal. Please transition to setupAsync.",
        ReplaceWith("asyncSetup(context, additionalActions)"))
fun <C : Any> setupAsync2(context: C, additionalActions: suspend C.() -> Unit = {}) = asyncSetup(context, additionalActions)

@Deprecated("Ready to promote this use case to normal. Please transition to setupAsync.",
        ReplaceWith("asyncSetup(contextProvider, additionalActions)"))
fun <C : Any> setupAsync2(contextProvider: suspend () -> C, additionalActions: suspend C.() -> Unit = {}) =
        asyncSetup(contextProvider, additionalActions)

object AsyncMints : AsyncMintDispatcher, ReporterProvider by MintReporterConfig