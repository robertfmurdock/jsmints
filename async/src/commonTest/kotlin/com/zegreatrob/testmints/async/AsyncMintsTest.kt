package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.CompoundMintTestException
import com.zegreatrob.testmints.captureException
import com.zegreatrob.testmints.report.MintReporter
import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

@Suppress("unused")
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

        @Test
        fun canFailAsyncWithCoroutine() = asyncSetup(object {

            fun testThatFailsWithCoroutine() = asyncSetup(Unit) exercise {
            } verify {
                withContext<Unit>(Dispatchers.Default) {
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

        @Test
        fun failuresThrownDuringVerifyWillFailTheTest() = asyncSetup(object {
            val assertionError = AssertionError("ExpectedAssertion ${Random.nextInt()}")

            fun testThatFailsDuringVerify(assertionError: AssertionError) = asyncSetup(object {
            }) exercise {
            } verify { throw assertionError }

        }) exercise {
            captureException { waitForTest { testThatFailsDuringVerify(assertionError) } }
        } verify { result ->
            assertEquals(assertionError.message, result?.message)
        }

        @Test
        fun failuresThrownDuringExerciseWillFailTheTest() = asyncSetup(object {
            val assertionError = AssertionError("ExpectedAssertion ${Random.nextInt()}")

            fun testThatFailsDuringExercise() = asyncSetup(object {
            }) exercise {
                throw assertionError
            } verify {}

        }) exercise {
            captureException { waitForTest { testThatFailsDuringExercise() } }
        } verify { result ->
            assertEquals(assertionError.message, result?.message)
        }

        @Test
        fun canUseDeferredDuringExerciseClosure() = asyncSetup(object {
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
        fun canUseSuspendFunctionsDuringExerciseClosure() = asyncSetup(object {
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
        fun canUseDeferredDuringVerifyClosure() = asyncSetup(object {
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
        fun canUseSuspendFunctionsDuringVerifyClosure() = asyncSetup(object {
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
        fun setupObjectCanBeCreatedInSuspendClosure() = asyncSetup({
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
        fun setupCanContinueInSuspendableClosureBeforeExercise() = asyncSetup(object {
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
        fun usingScopeMintWillProvideSetupScopeThatWillCompleteBeforeExercise() = nativeEventLoopWeirdnessProtection {
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

        private fun nativeEventLoopWeirdnessProtection(thing: () -> Unit) = eventLoopProtect(thing)

        @Test
        fun usingScopeMintWillProvideExerciseScopeThatWillCompleteBeforeVerify() = asyncSetup(object : ScopeMint() {
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

        @Test
        fun tearDownShouldHaveAccessToScopeOfSetupObjectAndResult() = asyncSetup(object {
            val expectedValue: Int = Random.nextInt()
            val expectedResult: Int = Random.nextInt()
            val valueCollector = mutableListOf<Pair<Int, Int>>()
        }) exercise {
            fun testThatSendsContextToTeardown() = asyncSetup(object {
                val value = expectedValue
            }) exercise {
                expectedResult
            } verifyAnd {
            } teardown { result ->
                valueCollector.add(value to result)
            }

            waitForTest { testThatSendsContextToTeardown() }
        } verify {
            assertEquals(expectedValue to expectedResult, valueCollector[0])
        }

        @Test
        fun whenFailureOccursInVerifyAndExceptionOccursInTeardownBothAreReported() = asyncSetup(object {
            val verifyFailure = AssertionError("Got 'em")
            val teardownException = Exception("Oh man, not good. ${Random.nextInt()}")

            fun failingTestThatExplodesInTeardown() = asyncSetup(object {}) exercise {
            } verifyAnd { throw verifyFailure } teardown { throw teardownException }

        }) exercise {
            captureException { waitForTest { failingTestThatExplodesInTeardown() } }
        } verify { result ->
            when (result) {
                is CompoundMintTestException -> {
                    assertEquals(verifyFailure.message, result.exceptions["Failure"]?.message)
                    assertEquals(teardownException.message, result.exceptions["Teardown exception"]?.message)
                }
                else -> fail("was not correct exception type.")
            }
        }

        @Test
        fun whenExceptionOccursInSetupClosureWillNotRunExerciseOrTeardown() = asyncSetup(object {
            val setupException = Exception("Oh man, not good. ${Random.nextInt()}")
            var exerciseOrVerifyTriggered = false

            fun testThatExplodeInSetupClosure() = asyncSetup(Unit) {
                throw setupException
            } exercise { exerciseOrVerifyTriggered = true } verify { exerciseOrVerifyTriggered = true }

        }) exercise {
            captureException { waitForTest { testThatExplodeInSetupClosure() } }
        } verify { result ->
            assertEquals(setupException.message, result?.message)
        }

        class TestTemplates {
            enum class Steps {
                TemplateSetup, TemplateTeardown, Setup, Exercise, Verify, Teardown
            }

            private val correctOrder = listOf(
                    Steps.TemplateSetup,
                    Steps.Setup,
                    Steps.Exercise,
                    Steps.Verify,
                    Steps.Teardown,
                    Steps.TemplateTeardown
            )

            @Test
            fun whenTestSucceedsSharedSetupAndSharedTeardownRunInCorrectOrder() = asyncSetup(object {
                val calls = mutableListOf<Steps>()
                val customSetup = asyncTestTemplate(
                        sharedSetup = { calls.add(Steps.TemplateSetup) },
                        sharedTeardown = { calls.add(Steps.TemplateTeardown) }
                )

                fun testThatSucceeds() = customSetup(object {}) { calls.add(Steps.Setup) }
                        .exercise { calls.add(Steps.Exercise) }
                        .verifyAnd { calls.add(Steps.Verify) }
                        .teardown { calls.add(Steps.Teardown) }

            }) exercise {
                waitForTest { testThatSucceeds() }
            } verify {
                assertEquals(correctOrder, calls)
            }

            @Test
            fun whenTestSucceedsEndingWithVerifySharedSetupAndSharedTeardownRunInCorrectOrder() = asyncSetup(object {
                val calls = mutableListOf<Steps>()
                val customSetup = asyncTestTemplate(
                        sharedSetup = { calls.add(Steps.TemplateSetup) },
                        sharedTeardown = { calls.add(Steps.TemplateTeardown) }
                )

                fun testThatSucceeds() = customSetup(object {}) { calls.add(Steps.Setup) }
                        .exercise { calls.add(Steps.Exercise) }
                        .verify { calls.add(Steps.Verify) }

            }) exercise {
                waitForTest { testThatSucceeds() }
            } verify {
                assertEquals(correctOrder - Steps.Teardown, calls)
            }

            @Test
            fun testTemplateCanBeExtendedByCallingTestTemplateAgain() = asyncSetup(object {
                val calls = mutableListOf<String>()
                val customSetup = asyncTestTemplate(
                        sharedSetup = { calls.add("setup") }, sharedTeardown = { calls.add("teardown") }
                )

                val bolsteredCustomSetup = customSetup.extend(
                        sharedSetup = { calls.add("inner setup") }, sharedTeardown = { calls.add("inner teardown") }
                )

                fun test() = bolsteredCustomSetup(object {}) exercise {} verify {}
            }) exercise {
                waitForTest { test() }
            } verify {
                assertEquals(listOf("setup", "inner setup", "inner teardown", "teardown"), calls)
            }

            @Test
            fun whenVerifyFailsSharedSetupAndSharedTeardownRunInCorrectOrder() = asyncSetup(object {
                val calls = mutableListOf<Steps>()
                val customSetup = asyncTestTemplate(
                        sharedSetup = { calls.add(Steps.TemplateSetup) },
                        sharedTeardown = { calls.add(Steps.TemplateTeardown) }
                )

                fun testThatFails() = customSetup(object {}) { calls.add(Steps.Setup) }
                        .exercise { calls.add(Steps.Exercise) }
                        .verifyAnd { calls.add(Steps.Verify); fail("This test fails.") }
                        .teardown { calls.add(Steps.Teardown) }

            }) exercise {
                captureException { waitForTest { testThatFails() } }
            } verify {
                assertEquals(correctOrder, calls)
            }

            @Test
            fun whenExceptionOccursInTeardownAndInTemplateTeardownBothAreReported() = asyncSetup(object {
                val teardownException = Exception("Oh man, not good.")
                val templateTeardownException = Exception("Now we're really off-road")
                val customSetup = asyncTestTemplate(
                        sharedSetup = {}, sharedTeardown = { throw templateTeardownException }
                )

                fun failingTestThatExplodesInTeardown() = customSetup(object {}) exercise {} verifyAnd {
                } teardown { throw teardownException }

            }) exercise {
                captureException { waitForTest { failingTestThatExplodesInTeardown() } }
            } verify { result ->
                val expected = CompoundMintTestException(mapOf(
                        "Teardown exception" to teardownException,
                        "Template teardown exception" to templateTeardownException)
                )
                assertEquals(expected, result)
            }

            @Test
            fun sharedSetupCanReturnContextThatWillBeProvidedToTheTeardown() = asyncSetup(object {
                val int = Random.nextInt()
                val callArguments = mutableListOf<Any>()
                val customSetup = asyncTestTemplate(
                        sharedSetup = { int },
                        sharedTeardown = { callArguments.add(it) }
                )

                fun testThatSucceeds() = customSetup(object {}) { }
                        .exercise { }
                        .verify { }

            }) exercise {
                waitForTest { testThatSucceeds() }
            } verify {
                assertEquals(listOf<Any>(int), callArguments)
            }

        }

    }

    class ReporterFeatures {

        enum class Call {
            ExerciseStart, ExerciseFinish, VerifyStart, VerifyFinish, TeardownStart, TeardownFinish
        }

        @Test
        fun willReportTestEventInOrderToReporter() = asyncSetup(object : AsyncMintDispatcher {
            val calls = mutableListOf<Call>()
            private fun record(call: Call) = calls.add(call).let { Unit }

            override val reporter = object : MintReporter {
                override fun exerciseStart(context: Any) = record(Call.ExerciseStart)
                override fun exerciseFinish() = record(Call.ExerciseFinish)
                override fun verifyStart(payload: Any?) = record(Call.VerifyStart)
                override fun verifyFinish() = record(Call.VerifyFinish)
                override fun teardownStart() = record(Call.TeardownStart)
                override fun teardownFinish() = record(Call.TeardownFinish)
            }

            fun exampleTest() = asyncSetup(object {}) exercise {} verifyAnd {} teardown {}

        }) exercise {
            waitForTest { exampleTest() }
        } verify {
            assertEquals(
                    expected = listOf(
                            Call.ExerciseStart,
                            Call.ExerciseFinish,
                            Call.VerifyStart,
                            Call.VerifyFinish,
                            Call.TeardownStart,
                            Call.TeardownFinish
                    ),
                    actual = calls
            )
        }

        @Test
        fun exerciseStartWillLogContext() = asyncSetup(object : AsyncMintDispatcher {
            var exerciseStartPayload: Any? = null
            override val reporter = object : MintReporter {
                override fun exerciseStart(context: Any) {
                    exerciseStartPayload = context
                }
            }
            val expectedObject = object {}

            fun simpleTest() = asyncSetup(expectedObject) exercise { } verify {}

        }) exercise {
            waitForTest { simpleTest() }
        } verify {
            assertEquals(expectedObject, exerciseStartPayload)
        }

        @Test
        fun verifyStartWillLogThePayload() = asyncSetup(object : AsyncMintDispatcher {
            var verifyStartPayload: Any? = null

            override val reporter = object : MintReporter {
                override fun verifyStart(payload: Any?) {
                    verifyStartPayload = payload
                }
            }
            val expectedResult = object {}
            fun simpleTest() = asyncSetup(object {}) exercise { expectedResult } verify {}

        }) exercise {
            waitForTest { simpleTest() }
        } verify {
            assertEquals(expectedResult, verifyStartPayload)
        }
    }

}
