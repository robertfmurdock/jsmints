package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.MintReporter

class TestTemplate<SC : Any>(val reporter: MintReporter, val wrapper: ((SC) -> Unit) -> Unit) {

    operator fun <C : Any> invoke(contextProvider: (SC) -> C, additionalSetupActions: C.() -> Unit = {}) =
            Setup(contextProvider, reporter, additionalSetupActions, wrapper)

    fun <SC2 : Any> extend(wrapper: (SC, (SC2) -> Unit) -> Unit) = TestTemplate<SC2>(reporter) { test ->
        this.wrapper { sc1 -> wrapper(sc1, test) }
    }

    fun <SC2 : Any> extend(sharedSetup: (SC) -> SC2, sharedTeardown: (SC2) -> Unit = {}) = extend<SC2> { sc1, test ->
        val sc2 = sharedSetup(sc1)
        test(sc2)
        sharedTeardown(sc2)
    }

    fun extend(sharedSetup: () -> Unit = {}, sharedTeardown: () -> Unit = {}) = TestTemplate<SC>(
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

operator fun <SC : Any, C : Any> TestTemplate<SC>.invoke(
        context: C,
        additionalSetupActions: C.() -> Unit = {}
) = Setup({ context }, reporter, additionalSetupActions, wrapper)
