package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.report.MintReporter
import com.zegreatrob.testmints.report.MintReporterConfig
import com.zegreatrob.testmints.report.ReporterProvider

interface AsyncMintDispatcher : SetupSyntax

interface SetupSyntax : ReporterProvider {
    fun <C : Any> asyncSetup(context: C, additionalActions: suspend C.() -> Unit = {}) = Setup(
            { context },
            context.chooseTestScope(),
            additionalActions,
            reporter,
            {},
            {}
    )

    fun <C : Any> asyncSetup(contextProvider: suspend () -> C, additionalActions: suspend C.() -> Unit = {}) = Setup(
            { contextProvider() },
            mintScope(),
            additionalActions,
            reporter,
            {},
            {}
    )

    fun asyncTestTemplate(sharedSetup: suspend () -> Unit, sharedTeardown: suspend () -> Unit) = TestTemplate(
            sharedSetup, { sharedTeardown() }, reporter
    )

    fun <SC : Any> asyncTestTemplate(sharedSetup: suspend () -> SC, sharedTeardown: suspend (SC) -> Unit) = TestTemplate(
            sharedSetup, sharedTeardown, reporter
    )
}

private fun Any.chooseTestScope() = if (this is ScopeMint) testScope else mintScope()

fun <C : Any> asyncSetup(context: C, additionalActions: suspend C.() -> Unit = {}) = AsyncMints
        .asyncSetup(context, additionalActions)

fun <C : Any> asyncSetup(contextProvider: suspend () -> C, additionalActions: suspend C.() -> Unit = {}) = AsyncMints
        .asyncSetup(contextProvider, additionalActions)

fun <SC : Any> asyncTestTemplate(sharedSetup: suspend () -> SC, sharedTeardown: suspend (SC) -> Unit) =
        AsyncMints.asyncTestTemplate(sharedSetup, sharedTeardown)

fun asyncTestTemplate(sharedSetup: suspend () -> Unit, sharedTeardown: suspend () -> Unit) =
        AsyncMints.asyncTestTemplate(sharedSetup, { sharedTeardown() })

@Deprecated("Ready to promote this use case to normal. Please transition to setupAsync.",
        ReplaceWith("asyncSetup(context, additionalActions)"))
fun <C : Any> setupAsync2(context: C, additionalActions: suspend C.() -> Unit = {}) = asyncSetup(context, additionalActions)

@Deprecated("Ready to promote this use case to normal. Please transition to setupAsync.",
        ReplaceWith("asyncSetup(contextProvider, additionalActions)"))
fun <C : Any> setupAsync2(contextProvider: suspend () -> C, additionalActions: suspend C.() -> Unit = {}) =
        asyncSetup(contextProvider, additionalActions)

object AsyncMints : AsyncMintDispatcher, ReporterProvider by MintReporterConfig

class TestTemplate<SC : Any>(
        val templateSetup: suspend () -> SC,
        val templateTeardown: suspend (SC) -> Unit,
        val reporter: MintReporter) {

    fun extend(sharedSetup: suspend () -> Unit, sharedTeardown: suspend () -> Unit = {}) = TestTemplate(
            templateSetup = { templateSetup().also { sharedSetup() } },
            templateTeardown = { sharedTeardown(); templateTeardown(it) },
            reporter = reporter
    )
}

operator fun <C : Any> TestTemplate<Unit>.invoke(
        contextProvider: suspend () -> C,
        additionalAction: suspend C.() -> Unit = {}
): Setup<C, Unit> {
    val unitSharedContextAdapter: suspend (Unit) -> C = { contextProvider() }
    return invoke(unitSharedContextAdapter, additionalAction)
}

operator fun <SC : Any, C : Any> TestTemplate<SC>.invoke(
        context: C,
        additionalActions: suspend C.() -> Unit = {}
) = Setup(
        { context },
        context.chooseTestScope(),
        additionalActions,
        reporter,
        templateSetup,
        templateTeardown
)

operator fun <SC : Any, C : Any> TestTemplate<SC>.invoke(
        contextProvider: suspend (SC) -> C,
        additionalActions: suspend C.() -> Unit = {}
) = Setup(
        contextProvider,
        mintScope(),
        additionalActions,
        reporter,
        templateSetup,
        templateTeardown
)