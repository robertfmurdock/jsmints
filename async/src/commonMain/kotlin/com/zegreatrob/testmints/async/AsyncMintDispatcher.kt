package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.report.MintReporterConfig
import com.zegreatrob.testmints.report.ReporterProvider
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.jvm.JvmName

interface AsyncMintDispatcher : SetupSyntax

interface SetupSyntax : ReporterProvider {

    val asyncSetup get() = TestTemplate<Unit>(reporter, mintScope()) { it(Unit) }

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

    fun <SC : Any> asyncTestTemplate(wrapper: suspend (TestFunc<SC>) -> Unit) = TestTemplate(
        reporter, mintScope(), wrapper
    )
}

internal fun Any.chooseTestScope() = if (this is ScopeMint) testScope else mintScope()

val asyncSetup get() = AsyncMints.asyncSetup

fun <SC : Any> asyncTestTemplate(sharedSetup: suspend () -> SC, sharedTeardown: suspend (SC) -> Unit = {}) =
    AsyncMints.asyncTestTemplate(sharedSetup, sharedTeardown)

fun <SC : Any> asyncTestTemplate(beforeAll: suspend () -> SC) = AsyncMints.asyncTestTemplate(beforeAll = beforeAll)

fun asyncTestTemplate(sharedSetup: suspend () -> Unit, sharedTeardown: suspend () -> Unit) =
    AsyncMints.asyncTestTemplate(sharedSetup, { sharedTeardown() })

@JvmName("asyncTestTemplateSimple")
fun asyncTestTemplate(wrapper: suspend (suspend () -> Unit) -> Unit) = AsyncMints.asyncTestTemplateSimple(wrapper)

@JvmName("asyncTestTemplateSC")
fun <SC : Any> asyncTestTemplate(wrapper: suspend (TestFunc<SC>) -> Unit) =
    AsyncMints.asyncTestTemplate(wrapper)

object AsyncMints : AsyncMintDispatcher, ReporterProvider by MintReporterConfig
