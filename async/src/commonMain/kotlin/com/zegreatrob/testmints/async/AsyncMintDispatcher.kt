package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.report.MintReporter
import com.zegreatrob.testmints.report.MintReporterConfig
import com.zegreatrob.testmints.report.ReporterProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
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

    fun <SC : Any> asyncTestTemplate(beforeAll: suspend () -> SC): TestTemplate<SC> {
        val templateScope = mintScope()
        val deferred: Deferred<SC> = templateScope.async(start = CoroutineStart.LAZY) { beforeAll() }
        return TestTemplate(reporter) { it(deferred.await()) }
    }

    fun asyncTestTemplateSimple(wrapper: suspend (suspend () -> Unit) -> Unit) = TestTemplate<Unit>(reporter) {
        wrapper { it(Unit) }
    }

    fun <SC : Any> asyncTestTemplate(wrapper: suspend (suspend (SC) -> Unit) -> Unit) = TestTemplate(
            reporter, mintScope(), wrapper
    )
}

private fun Any.chooseTestScope() = if (this is ScopeMint) testScope else mintScope()

fun <C : Any> asyncSetup(contextProvider: suspend () -> C, additionalActions: suspend C.() -> Unit = {}) = AsyncMints
        .asyncSetup(contextProvider, additionalActions)

fun <C : Any> asyncSetup(context: C, additionalActions: suspend C.() -> Unit = {}) = AsyncMints
        .asyncSetup(context, additionalActions)

fun <SC : Any> asyncTestTemplate(sharedSetup: suspend () -> SC, sharedTeardown: suspend (SC) -> Unit = {}) =
        AsyncMints.asyncTestTemplate(sharedSetup, sharedTeardown)

fun <SC : Any> asyncTestTemplate(beforeAll: suspend () -> SC) =
        AsyncMints.asyncTestTemplate(beforeAll = beforeAll)

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
        val templateScope: CoroutineScope = mintScope(),
        val wrapper: suspend (suspend (SC) -> Unit) -> Unit
) {

    fun extend(sharedSetup: suspend () -> Unit = {}, sharedTeardown: suspend () -> Unit = {}) = TestTemplate<SC>(
            reporter = reporter,
            wrapper = { test ->
                wrapper {
                    sharedSetup()
                    test(it)
                    sharedTeardown()
                }
            }
    )

    fun <SC2 : Any> extend(
            wrapper: suspend ((SC, suspend (SC2) -> Unit) -> Unit)
    ) = TestTemplate<SC2>(reporter) { test ->
        this.wrapper { sc1 -> wrapper(sc1, test) }
    }

    fun <SC2 : Any> extend(
            sharedSetup: suspend (SC) -> SC2,
            sharedTeardown: suspend (SC2) -> Unit = {}
    ) = extend<SC2> { sc1, test ->
        val sc2 = sharedSetup(sc1)
        test(sc2)
        sharedTeardown(sc2)
    }

    fun <BAC : Any> extend(beforeAll: suspend () -> BAC): TestTemplate<BAC> = extend(
            beforeAll = beforeAll,
            mergeContext = { _, bac -> bac }
    )

    fun <BAC : Any, SC2 : Any> extend(
            beforeAll: suspend () -> BAC,
            mergeContext: suspend (SC, BAC) -> SC2
    ): TestTemplate<SC2> {
        val deferred = templateScope.async(start = CoroutineStart.LAZY) { beforeAll() }
        return extend(sharedSetup = { sharedContext ->
            mergeContext(sharedContext, deferred.await())
        })
    }

    operator fun <C : Any> invoke(
            contextProvider: suspend (SC) -> C,
            additionalActions: suspend C.() -> Unit = {}
    ) = Setup(
            contextProvider,
            mintScope(),
            additionalActions,
            reporter,
            wrapper
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

operator fun <C : Any> TestTemplate<C>.invoke(additionalActions: suspend C.() -> Unit = {}) = Setup(
        { it },
        mintScope(),
        additionalActions,
        reporter,
        wrapper
)
