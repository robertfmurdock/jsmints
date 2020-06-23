package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.MintReporter
import com.zegreatrob.testmints.report.ReporterProvider


interface StandardMintDispatcher : ReporterProvider {

// Vernacular based on http://xunitpatterns.com/Four%20Phase%20Test.html

    fun <C : Any> setup(context: C, additionalSetupActions: C.() -> Unit = {}) = Setup<C, Unit>(
            { context }, reporter, additionalSetupActions, { it(Unit) }
    )

    fun <C : Any> setup(context: () -> C, additionalSetupActions: C.() -> Unit = {}) = Setup<C, Unit>(
            { context() }, reporter, additionalSetupActions, { it(Unit) }
    )

    fun <SC : Any> testTemplate(sharedSetup: () -> SC, sharedTeardown: (SC) -> Unit) = TestTemplate<SC>(
            reporter
    ) {
        val sc = sharedSetup()
        it(sc)
        sharedTeardown(sc)
    }

    fun testTemplateSimple(wrapper: (() -> Unit) -> Unit): TestTemplate<Unit> = TestTemplate(reporter) {
        wrapper { it(Unit) }
    }

    fun <SC : Any> testTemplate(wrapper: ((SC) -> Unit) -> Unit): TestTemplate<SC> = TestTemplate(reporter, wrapper)

}

class TestTemplate<SC : Any>(
        private val reporter: MintReporter,
        private val wrapper: ((SC) -> Unit) -> Unit
) {
    operator fun <C : Any> invoke(context: C, additionalSetupActions: C.() -> Unit = {}) =
            Setup({ context }, reporter, additionalSetupActions, wrapper)

    operator fun <C : Any> invoke(contextProvider: (SC) -> C, additionalSetupActions: C.() -> Unit = {}) =
            Setup(contextProvider, reporter, additionalSetupActions, wrapper)

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

    fun <SC2 : Any> extend(wrapper: (SC, (SC2) -> Unit) -> Unit) = TestTemplate<SC2>(reporter) { test ->
        this.wrapper { sc1 -> wrapper(sc1, test) }
    }

    fun <SC2 : Any> extend(sharedSetup: (SC) -> SC2, sharedTeardown: (SC2) -> Unit = {}) = extend<SC2> { sc1, test ->
        val sc2 = sharedSetup(sc1)
        test(sc2)
        sharedTeardown(sc2)
    }

}

typealias ExerciseFunc<C, R> = C.() -> R
typealias VerifyFunc<C, R> = C.(R) -> Any
typealias TeardownFunc<C, R> = C.(R) -> Unit

