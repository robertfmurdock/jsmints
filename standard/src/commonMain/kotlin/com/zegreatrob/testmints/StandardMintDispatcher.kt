package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.ReporterProvider


interface StandardMintDispatcher : ReporterProvider {

    val setup get() = TestTemplate<Unit>(this) { it(Unit) }

    fun <SC : Any> testTemplate(wrapper: (TestFunc<SC>) -> Unit): TestTemplate<SC> = TestTemplate(this, wrapper)

    fun <SC : Any> testTemplate(sharedSetup: () -> SC, sharedTeardown: (SC) -> Unit = {}) = testTemplate<SC> { test ->
        sharedSetup()
            .also(test)
            .also(sharedTeardown)
    }

    fun <SC : Any> testTemplate(beforeAll: () -> SC): TestTemplate<SC> {
        val lazy by lazy { beforeAll() }
        return testTemplate(wrapper = { it(lazy) })
    }

    fun testTemplateSimple(wrapper: (() -> Unit) -> Unit): TestTemplate<Unit> =
        testTemplate(wrapper = { wrapper { it(Unit) } })

}

typealias ExerciseFunc<C, R> = C.() -> R
typealias VerifyFunc<C, R> = C.(R) -> Any
typealias TeardownFunc<C, R> = C.(R) -> Unit

