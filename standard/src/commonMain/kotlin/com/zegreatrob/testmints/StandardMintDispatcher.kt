package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.ReporterProvider


interface StandardMintDispatcher : ReporterProvider {

// Vernacular based on http://xunitpatterns.com/Four%20Phase%20Test.html

    fun <C : Any> setup(context: C, additionalSetupActions: C.() -> Unit = {}) = Setup<C, Unit>(
        { context }, reporter, additionalSetupActions, { it(Unit) }
    )

    fun <C : Any> setup(context: () -> C, additionalSetupActions: C.() -> Unit = {}) = Setup<C, Unit>(
        { context() }, reporter, additionalSetupActions, { it(Unit) }
    )

    fun <SC : Any> testTemplate(wrapper: ((SC) -> Unit) -> Unit): TestTemplate<SC> = TestTemplate(reporter, wrapper)

    fun <SC : Any> testTemplate(sharedSetup: () -> SC, sharedTeardown: (SC) -> Unit) = testTemplate<SC> { test ->
        sharedSetup()
            .also(test)
            .also(sharedTeardown)
    }

    fun testTemplateSimple(wrapper: (() -> Unit) -> Unit): TestTemplate<Unit> = testTemplate { wrapper { it(Unit) } }

}

typealias ExerciseFunc<C, R> = C.() -> R
typealias VerifyFunc<C, R> = C.(R) -> Any
typealias TeardownFunc<C, R> = C.(R) -> Unit

