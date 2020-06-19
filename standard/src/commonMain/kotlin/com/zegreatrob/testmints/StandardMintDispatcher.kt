package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.MintReporter
import com.zegreatrob.testmints.report.ReporterProvider


interface StandardMintDispatcher : ReporterProvider {

// Vernacular based on http://xunitpatterns.com/Four%20Phase%20Test.html

    fun <C : Any> setup(context: C, additionalSetupActions: C.() -> Unit = {}) = Setup(
            context, reporter, additionalSetupActions, {}
    )

    fun <SC : Any> testTemplate(sharedSetup: () -> SC, sharedTeardown: (SC) -> Unit) = TestTemplate(
            sharedSetup, sharedTeardown, reporter
    )

    fun testTemplate(wrapper: (() -> Unit) -> Unit): TestTemplate<Unit> = TestTemplate(
            { }, { }, reporter, wrapper
    )

}

class TestTemplate<SC : Any>(
        private val templateSetup: () -> SC,
        private val templateTeardown: (SC) -> Unit,
        private val reporter: MintReporter,
        private val wrapper: (() -> Unit) -> Unit = { it() }
) {
    operator fun <C : Any> invoke(context: C, additionalSetupActions: C.() -> Unit = {}) =
            Setup(context, reporter, additionalSetupActions, templateSetup, templateTeardown, wrapper)

    fun extend(sharedSetup: () -> Unit = {}, sharedTeardown: () -> Unit = {}) = TestTemplate(
            templateSetup = { templateSetup().also { sharedSetup() } },
            templateTeardown = { sharedTeardown(); templateTeardown(it) },
            reporter = reporter,
            wrapper = wrapper
    )
}

typealias ExerciseFunc<C, R> = C.() -> R
typealias VerifyFunc<C, R> = C.(R) -> Any
typealias TeardownFunc<C, R> = C.(R) -> Unit

