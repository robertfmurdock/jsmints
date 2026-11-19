package com.zegreatrob.wrapper.playwright

import com.zegreatrob.testmints.async.Exercise
import com.zegreatrob.testmints.async.Setup
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.test.TestResult
import kotlin.js.Promise

fun <C : Any> playwrightSetup(
    timeoutMs: Long = 60_000,
    setup: suspend (Page) -> C,
) = playwrightSetupFrom { page ->
    asyncSetup.with({ setup(page) }, timeoutMs = timeoutMs)
}

/** Adapts an existing Testmints setup, including a setup built from a custom template. */
fun <C : Any> playwrightSetupFrom(
    run: ((Page) -> TestResult) -> Promise<Unit> = { scenario -> playwrightTest(scenario = scenario) },
    setup: (Page) -> Setup<C, *>,
) = PlaywrightSetup(run, setup)

class PlaywrightSetup<C : Any> internal constructor(
    private val run: ((Page) -> TestResult) -> Promise<Unit>,
    private val setup: (Page) -> Setup<C, *>,
) {
    infix fun <R> exercise(exercise: suspend C.() -> R) = PlaywrightExercise(run) { page ->
        setup(page) exercise exercise
    }
}

class PlaywrightExercise<C : Any, R> internal constructor(
    private val run: ((Page) -> TestResult) -> Promise<Unit>,
    private val exercise: (Page) -> Exercise<C, R>,
) {
    infix fun verify(verify: suspend C.(R) -> Unit) = run { page -> exercise(page) verify verify }

    infix fun verifyAnd(verify: suspend C.(R) -> Unit) = PlaywrightVerify<C, R> { teardown ->
        run { page ->
            exercise(page) verifyAnd verify teardown teardown
        }
    }
}

class PlaywrightVerify<C : Any, R> internal constructor(
    private val run: (suspend C.(R?) -> Unit) -> Promise<Unit>,
) {
    infix fun teardown(teardown: suspend C.(R?) -> Unit) = run(teardown)
}
