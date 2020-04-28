package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.report.MintReporter
import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class Async2Test {

    @Test
    fun example() = setupAsync2(object {
        val thing = 1
    }) exercise {
        thing + 1
    } verify { result ->
        assertEquals(2, result)
    }

    @Test
    fun willFailVerifyCorrectly() = setupAsync2(object {
        val assertionError = AssertionError("ExpectedAssertion ${Random.nextInt()}")
    }) exercise {
        try {
            testThatFailsDuringVerify(assertionError)
            fail("The test should fail.")
        } catch (bad: Throwable) {
            bad
        }
    } verify { result: Throwable ->
        assertEquals(result.message, assertionError.message)
    }

    private suspend fun testThatFailsDuringVerify(assertionError: AssertionError) = waitForTest {
        setupAsync2(object {
        }) exercise {
        } verify {
            throw assertionError
        }
    }

    @Test
    fun willFailExerciseCorrectly() = setupAsync2(object {
        val assertionError = AssertionError("ExpectedAssertion ${Random.nextInt()}")
    }) exercise {
        try {
            testThatFailsDuringExercise(assertionError)
            fail("The test should fail.")
        } catch (bad: Throwable) {
            bad
        }
    } verify { result: Throwable ->
        assertEquals(result.message, assertionError.message)
    }

    private suspend fun testThatFailsDuringExercise(assertionError: AssertionError) = waitForTest {
        setupAsync2(object {
        }) exercise {
            throw assertionError
        } verify {
        }
    }

    @Test
    fun willSupportDeferredInExercise() = setupAsync2(object {
        val expected = Random.nextInt()
        val asyncGuy = object {
            fun doThingAsync() = CompletableDeferred(expected)
        }
    }) exercise {
        asyncGuy.doThingAsync().await()
    } verify { result: Int ->
        assertEquals(expected, result)
    }

    @Test
    fun willSupportSuspendInExercise() = setupAsync2(object {
        val expected = Random.nextInt()
        val asyncGuy = object {
            suspend fun doThingAsync() = CompletableDeferred(expected).await()
        }
    }) exercise {
        asyncGuy.doThingAsync()
    } verify { result: Int ->
        assertEquals(expected, result)
    }

    @Test
    fun willSupportDeferredInVerify() = setupAsync2(object {
        val expected = Random.nextInt()
    }) exercise {
        object {
            fun doThingAsync() = CompletableDeferred(expected)
        }
    } verify { asyncGuy ->
        val result = asyncGuy.doThingAsync().await()
        assertEquals(expected, result)
    }

    @Test
    fun willSupportSuspendInVerify() = setupAsync2(object {
        val expected = Random.nextInt()
    }) exercise {
        object {
            suspend fun doThingAsync() = CompletableDeferred(expected).await()
        }
    } verify { asyncGuy ->
        val result = asyncGuy.doThingAsync()
        assertEquals(expected, result)
    }

    @Test
    fun willSupportSuspendObjectInitializationWhichIsUsefulForHelperMethods() = setupAsync2({
        val asyncProducedValue = CompletableDeferred(Random.nextInt()).await()
        object {
            val asyncProducedValue = asyncProducedValue
        }
    }) exercise {
        asyncProducedValue
    } verify { result ->
        assertEquals(asyncProducedValue, result)
    }

    @Test
    fun willSupportAfterObjectAsyncSetup() = setupAsync2(object {
        val coolString = "${Random.nextDouble()}"
        var list = mutableListOf<String>()
    }) {
        val asyncProducedValue = CompletableDeferred("$coolString And ${Random.nextInt()}").await()
        list.add(asyncProducedValue)
    } exercise {
        list.apply { shuffle() }
    } verify { result ->
        assertEquals(1, result.size)
    }

    @Test
    fun canProvideScopeUsingScopeMintSetupScopeWillCompleteBeforeExercise() = eventLoopProtect {
        setupAsync2(object : ScopeMint() {
            val expectedValue = Random.nextInt()
            val asyncProducedValue = setupScope.async { delay(40); expectedValue }
        }) exercise {
            asyncProducedValue.isCompleted
        } verify { setupAsyncCompletedBeforeExercise ->
            assertEquals(true, setupAsyncCompletedBeforeExercise)
            assertEquals(expectedValue, asyncProducedValue.await())
        }
    }

    @Test
    fun canProvideScopeUsingScopeMintExerciseScopeWillCompleteBeforeVerify() = setupAsync2(object : ScopeMint() {
        val expectedValue = Random.nextInt()
    }) exercise {
        exerciseScope.async { delay(40); expectedValue }
    } verify { result ->
        assertEquals(true, result.isCompleted)
        assertEquals(expectedValue, result.await())
    }

    @Test
    fun canMakeScopeInExerciseThatWillCompleteBeforeVerify() = setupAsync2(object : ScopeMint() {
        val expectedValue = Random.nextInt()
    }) exercise {
        coroutineScope {
            async { delay(40); expectedValue }
        }
    } verify { result ->
        assertEquals(true, result.isCompleted)
        assertEquals(expectedValue, result.await())
    }

    class ReporterFeatures {

        enum class Call {
            ExerciseStart, ExerciseFinish, VerifyStart, VerifyFinish, TeardownStart, TeardownFinish
        }

        @Test
        fun willReportTestEventInOrderToReporter() = testAsync {
            val reporter = object : MintReporter {
                val calls = mutableListOf<Call>()
                override fun exerciseStart(context: Any) = record(Call.ExerciseStart)
                override fun exerciseFinish() = record(Call.ExerciseFinish)
                override fun verifyStart(payload: Any?) = record(Call.VerifyStart)
                override fun verifyFinish() = record(Call.VerifyFinish)
                override fun teardownStart() = record(Call.TeardownStart)
                override fun teardownFinish() = record(Call.TeardownFinish)
                private fun record(call: Call) = calls.add(call).let { Unit }
            }

            object : AsyncMintDispatcher {
                override val reporter = reporter
            }.run {
                waitForTest { setupAsync2(object {}) exercise {} verifyAnd {} teardown {} }
            }

            assertEquals(
                    expected = listOf(
                            Call.ExerciseStart,
                            Call.ExerciseFinish,
                            Call.VerifyStart,
                            Call.VerifyFinish,
                            Call.TeardownStart,
                            Call.TeardownFinish
                    ),
                    actual = reporter.calls
            )
        }

        @Test
        fun exerciseStartWillLogContext() = testAsync {
            val reporter = object : MintReporter {
                var exerciseStartPayload: Any? = null
                override fun exerciseStart(context: Any) {
                    exerciseStartPayload = context
                }
            }

            val expectedObject = object {}

            object : AsyncMintDispatcher {
                override val reporter = reporter
            }.run {
                waitForTest { setupAsync2(expectedObject) exercise { } verify {} }
            }

            assertEquals(expectedObject, reporter.exerciseStartPayload)
        }

        @Test
        fun verifyStartWillLogThePayload() = testAsync {
            val reporter = object : MintReporter {
                var verifyStartPayload: Any? = null
                override fun verifyStart(payload: Any?) {
                    verifyStartPayload = payload
                }
            }

            val expectedObject = object {}

            object : AsyncMintDispatcher {
                override val reporter = reporter
            }.run {
                waitForTest { setupAsync2(object {}) exercise { expectedObject } verify {} }
            }

            assertEquals(expectedObject, reporter.verifyStartPayload)
        }
    }


}
