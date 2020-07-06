package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.report.MintReporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async

class TestTemplate<SC : Any>(
    val reporter: MintReporter,
    private val templateScope: CoroutineScope = mintScope(),
    val wrapper: suspend (TestFunc<SC>) -> Unit
) {

    fun <SC2 : Any> extend(wrapper: suspend (SC, TestFunc<SC2>) -> Unit) = TestTemplate<SC2>(reporter) { test ->
        this.wrapper { sc1 -> wrapper(sc1, test) }
    }

    fun <SC2 : Any> extend(sharedSetup: suspend (SC) -> SC2, sharedTeardown: suspend (SC2) -> Unit = {}) =
        extend<SC2> { sc1, test ->
            val sc2 = sharedSetup(sc1)
            test(sc2)
            sharedTeardown(sc2)
        }

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
    ) = Setup(contextProvider, mintScope(), additionalActions, reporter, wrapper)

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
