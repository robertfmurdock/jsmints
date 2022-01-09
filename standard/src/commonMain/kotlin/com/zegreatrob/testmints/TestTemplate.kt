package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.ReporterProvider

class TestTemplate<SC : Any>(val reporterProvider: ReporterProvider, val wrapper: (TestFunc<SC>) -> Unit) {

    fun <SC2 : Any> extend(wrapper: (SC, TestFunc<SC2>) -> Unit) = TestTemplate<SC2>(reporterProvider) { test ->
        this.wrapper { sc1 -> wrapper(sc1, test) }
    }

    fun <SC2 : Any> extend(sharedSetup: (SC) -> SC2, sharedTeardown: (SC2) -> Unit = {}) = extend<SC2> { sc1, test ->
        val sc2 = sharedSetup(sc1)
        test(sc2)
        sharedTeardown(sc2)
    }

    fun extend(sharedSetup: () -> Unit = {}, sharedTeardown: () -> Unit = {}) = TestTemplate<SC>(
        reporterProvider
    ) { test ->
        wrapper {
            sharedSetup()
            test(it)
            sharedTeardown()
        }
    }

    fun <BAC : Any> extend(beforeAll: () -> BAC): TestTemplate<BAC> = extend(
        beforeAll = beforeAll,
        mergeContext = { _, bac -> bac }
    )

    fun <BAC : Any, SC2 : Any> extend(beforeAll: () -> BAC, mergeContext: (SC, BAC) -> SC2): TestTemplate<SC2> {
        val lazy by lazy { beforeAll() }
        return TestTemplate(reporterProvider) { test -> wrapper { sc -> test(mergeContext(sc, lazy)) } }
    }

    operator fun <C : Any> invoke(contextProvider: (SC) -> C, additionalSetupActions: C.() -> Unit = {}) =
        Setup(contextProvider, reporterProvider.reporter, additionalSetupActions, wrapper)

    operator fun <C : Any> invoke(
        context: C,
        additionalSetupActions: C.() -> Unit = {}
    ) = Setup({ context }, reporterProvider.reporter, additionalSetupActions, wrapper)

    operator fun invoke(additionalSetupActions: SC.() -> Unit = {}): Setup<SC, SC> =
        Setup({ it }, reporterProvider.reporter, additionalSetupActions, wrapper)

}
