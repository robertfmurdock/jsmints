package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.report.MintReporter
import com.zegreatrob.testmints.report.MintReporterConfig
import com.zegreatrob.testmints.report.ReporterProvider
import kotlin.jvm.JvmName

interface AsyncMintDispatcher : SetupSyntax

interface SetupSyntax : ReporterProvider {
    fun <C : Any> asyncSetup(context: C, additionalActions: suspend C.() -> Unit = {}) = Setup<C, Unit>(
            { context },
            context.chooseTestScope(),
            additionalActions,
            reporter,
            { it(Unit) }
    )

    fun <C : Any> asyncSetup(
            contextProvider: suspend () -> C,
            additionalActions: suspend C.() -> Unit = {}
    ) = Setup<C, Unit>(
            { contextProvider() },
            mintScope(),
            additionalActions,
            reporter,
            { it(Unit) }
    )

    fun asyncTestTemplate(sharedSetup: suspend () -> Unit, sharedTeardown: suspend () -> Unit) = TestTemplate<Unit>(
            reporter
    ) {
        sharedSetup()
        it(Unit)
        sharedTeardown()
    }

    fun <SC : Any> asyncTestTemplate(
            sharedSetup: suspend () -> SC,
            sharedTeardown: suspend (SC) -> Unit = {}
    ) = TestTemplate<SC>(reporter) {
        val sc = sharedSetup()
        it(sc)
        sharedTeardown(sc)
    }

    fun asyncTestTemplateSimple(wrapper: suspend (suspend () -> Unit) -> Unit) = TestTemplate<Unit>(reporter) {
        wrapper { it(Unit) }
    }

    fun <SC : Any> asyncTestTemplate(wrapper: suspend (suspend (SC) -> Unit) -> Unit) = TestTemplate(
            reporter, wrapper
    )
}

private fun Any.chooseTestScope() = if (this is ScopeMint) testScope else mintScope()

fun <C : Any> asyncSetup(context: C, additionalActions: suspend C.() -> Unit = {}) = AsyncMints
        .asyncSetup(context, additionalActions)

fun <C : Any> asyncSetup(contextProvider: suspend () -> C, additionalActions: suspend C.() -> Unit = {}) = AsyncMints
        .asyncSetup(contextProvider, additionalActions)

fun <SC : Any> asyncTestTemplate(sharedSetup: suspend () -> SC, sharedTeardown: suspend (SC) -> Unit = {}) =
        AsyncMints.asyncTestTemplate(sharedSetup, sharedTeardown)

fun asyncTestTemplate(sharedSetup: suspend () -> Unit, sharedTeardown: suspend () -> Unit) =
        AsyncMints.asyncTestTemplate(sharedSetup, { sharedTeardown() })

@JvmName("asyncTestTemplateSimple")
fun asyncTestTemplate(wrapper: suspend (suspend () -> Unit) -> Unit) = AsyncMints.asyncTestTemplateSimple(wrapper)

@JvmName("asyncTestTemplateSC")
fun <SC : Any> asyncTestTemplate(wrapper: suspend (suspend (SC) -> Unit) -> Unit) = AsyncMints.asyncTestTemplate(wrapper)

@Deprecated("Ready to promote this use case to normal. Please transition to setupAsync.",
        ReplaceWith("asyncSetup(context, additionalActions)"))
fun <C : Any> setupAsync2(context: C, additionalActions: suspend C.() -> Unit = {}) = asyncSetup(context, additionalActions)

@Deprecated("Ready to promote this use case to normal. Please transition to setupAsync.",
        ReplaceWith("asyncSetup(contextProvider, additionalActions)"))
fun <C : Any> setupAsync2(contextProvider: suspend () -> C, additionalActions: suspend C.() -> Unit = {}) =
        asyncSetup(contextProvider, additionalActions)

object AsyncMints : AsyncMintDispatcher, ReporterProvider by MintReporterConfig

class TestTemplate<SC : Any>(
        val reporter: MintReporter,
        val wrapper: suspend (suspend (SC) -> Unit) -> Unit
) {

    fun extend(sharedSetup: suspend () -> Unit, sharedTeardown: suspend () -> Unit = {}) = TestTemplate<SC>(
            reporter = reporter,
            wrapper = { test ->
                wrapper {
                    sharedSetup()
                    test(it)
                    sharedTeardown()
                }
            }
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
        wrapper
)

operator fun <SC : Any, C : Any> TestTemplate<SC>.invoke(
        contextProvider: suspend (SC) -> C,
        additionalActions: suspend C.() -> Unit = {}
) = Setup(
        contextProvider,
        mintScope(),
        additionalActions,
        reporter,
        wrapper
)
