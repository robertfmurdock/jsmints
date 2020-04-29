package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.report.MintReporter
import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AsyncMintsTest {

    class ExampleUsage {

        private fun Int.plusOne() = this + 1

        @Test
        fun willOrganizeTestNicely() = asyncSetup(object {
            val input: Int = Random.nextInt()
            val expected = input + 1
        }) exercise {
            input.plusOne()
        } verify { result ->
            assertEquals(expected, result)
        }

        @Test
        fun suspendFunctionsCanBeUsedInSetupClosure() = asyncSetup(object {
            val input: Int = Random.nextInt()
            val expected = input + 1
            var databaseSetupCounter = 0
        }) {
            withContext(Dispatchers.Default) {
                delay(4)
                databaseSetupCounter++
            }
        } exercise {
            input + databaseSetupCounter
        } verify { result ->
            assertEquals(expected, result)
        }
    }

    class Features {

        @Test
        fun verifyWillCaptureFailures() = asyncSetup(object {
            fun testThatFails() = asyncSetup(object {
            }) exercise {
            } verify { fail("LOL") }
        }) exercise {
            captureException {
                waitForTest { testThatFails() }
            }
        } verify { result ->
            assertEquals("LOL", result?.message)
        }

        private suspend fun captureException(work: suspend () -> Unit) = try {
            work()
            null
        } catch (expectedFailure: Throwable) {
            expectedFailure
        }

        @Test
        fun canFailAsyncWithCoroutine() = asyncSetup(object {

            fun testThatFailsWithCoroutine() = asyncSetup(Unit) exercise {
            } verify {
                withContext<Nothing>(Dispatchers.Default) {
                    delay(3)
                    fail("LOL")
                }
            }

        }) exercise {
            captureException { waitForTest { testThatFailsWithCoroutine() } }
        } verify { result ->
            assertEquals("LOL", result?.message)
        }

        @Test
        fun verifyShouldThrowErrorWhenFailureOccurs() = asyncSetup(object {

            fun failingTest() = asyncSetup<Any>(object {
            }) exercise {
            } verify { fail("LOL") }

        }) exercise {
            captureException { waitForTest { failingTest() } }
        } verify { result ->
            assertEquals("LOL", result?.message)
        }

        @Test
        fun exerciseShouldHaveAccessToScopeOfSetupObject() = asyncSetup(object {
            val expectedValue: Int? = Random.nextInt()
            var actualValue: Int? = null

            fun testThatSharesScopeExample() = asyncSetup(object {
                @Suppress("UnnecessaryVariable")
                val value = expectedValue
            }) exercise {
                actualValue = value
            } verify {}

        }) exercise {
            waitForTest { testThatSharesScopeExample() }
        } verify {
            assertEquals(expectedValue, actualValue)
        }

        @Test
        fun verifyShouldReceiveTheResultOfExerciseAsParameter() = asyncSetup(object {
            val expectedValue = Random.nextInt()
            var actualValue: Int? = null

            fun testThatForwardsResultOfExerciseToVerify() = asyncSetup(object {
            }) exercise {
                expectedValue
            } verify { result ->
                actualValue = result
            }

        }) exercise {
            waitForTest { testThatForwardsResultOfExerciseToVerify() }
        } verify {
            assertEquals(expectedValue, actualValue)
        }

        @Test
        fun verifyShouldHaveAccessToScopeOfSetupObject() = asyncSetup(object {
            val expectedValue = Random.nextInt()
            var actualValue: Int? = null

            fun testThatUsesSetupObjectInVerify() = asyncSetup(object {
                val value = expectedValue
            }) exercise {
            } verify {
                actualValue = value
            }
        }) exercise {
            waitForTest { testThatUsesSetupObjectInVerify() }
        } verify {
            assertEquals(expectedValue, actualValue)
        }

    }

    @Test
    fun example() = asyncSetup(object {
        val thing = 1
    }) exercise {
        thing + 1
    } verify { result ->
        assertEquals(2, result)
    }

    @Test
    fun willFailVerifyCorrectly() = asyncSetup(object {
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
        asyncSetup(object {
        }) exercise {
        } verify {
            throw assertionError
        }
    }

    @Test
    fun willFailExerciseCorrectly() = asyncSetup(object {
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
        asyncSetup(object {
        }) exercise {
            throw assertionError
        } verify {
        }
    }

    @Test
    fun willSupportDeferredInExercise() = asyncSetup(object {
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
    fun willSupportSuspendInExercise() = asyncSetup(object {
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
    fun willSupportDeferredInVerify() = asyncSetup(object {
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
    fun willSupportSuspendInVerify() = asyncSetup(object {
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
    fun willSupportSuspendObjectInitializationWhichIsUsefulForHelperMethods() = asyncSetup({
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
    fun willSupportAfterObjectAsyncSetup() = asyncSetup(object {
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
        asyncSetup(object : ScopeMint() {
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
    fun canProvideScopeUsingScopeMintExerciseScopeWillCompleteBeforeVerify() = asyncSetup(object : ScopeMint() {
        val expectedValue = Random.nextInt()
    }) exercise {
        exerciseScope.async { delay(40); expectedValue }
    } verify { result ->
        assertEquals(true, result.isCompleted)
        assertEquals(expectedValue, result.await())
    }

    @Test
    fun canMakeScopeInExerciseThatWillCompleteBeforeVerify() = asyncSetup(object : ScopeMint() {
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
