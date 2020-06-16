package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.MintReporter
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

@Suppress("unused")
class TestMintsTest {

    class ExampleUsage {

        private fun Int.plusOne() = this + 1

        @Test
        fun simpleCase() = setup(object {
            val input: Int = Random.nextInt()
            val expected = input + 1
        }) exercise {
            input.plusOne()
        } verify { result ->
            assertEquals(expected, result)
        }

    }

    class Features {
        @Test
        fun verifyShouldThrowErrorWhenFailureOccurs() = setup(object {

            fun simulatedTestThatFailsInVerify(): Unit = setup(Unit) exercise {} verify { fail("LOL") }

        }) exercise {
            captureException { simulatedTestThatFailsInVerify() }
        } verify { result ->
            assertEquals("LOL", result?.message)
        }

        class ValueCollector(var actualValue: Int? = null)

        @Test
        fun exerciseShouldHaveAccessToScopeOfSetupObject() = setup(object {
            val expectedValue: Int? = Random.nextInt()
            val valueCollector = ValueCollector()

            fun testThatUsesContextInExercise() = setup(object {
                val value = expectedValue
            }) exercise {
                valueCollector.actualValue = value
            } verify {
            }

        }) exercise {
            testThatUsesContextInExercise()
        } verify {
            assertEquals(expectedValue, valueCollector.actualValue)
        }

        @Test
        fun verifyShouldReceiveTheResultOfExerciseAsParameter() = setup(object {
            val expectedValue = Random.nextInt()
            val valueCollector = ValueCollector()

            fun testThatPassesResultToVerify() = setup(object {}) exercise { expectedValue } verify { result ->
                valueCollector.actualValue = result
            }

        }) exercise {
            testThatPassesResultToVerify()
        } verify {
            assertEquals(expectedValue, valueCollector.actualValue)
        }

        @Test
        fun verifyShouldHaveAccessToSetupContext() = setup(object {
            val expectedValue: Int? = Random.nextInt()
            val valueCollector = ValueCollector()

            fun testThatUsesContextInVerify() = setup(object {
                val value = expectedValue
            }) exercise {} verify {
                valueCollector.actualValue = value
            }

        }) exercise {
            testThatUsesContextInVerify()
        } verify {
            assertEquals(expectedValue, valueCollector.actualValue)
        }

        @Test
        fun tearDownShouldHaveAccessToScopeOfSetupObjectAndResult() = setup(object {
            val expectedValue: Int = Random.nextInt()
            val expectedResult: Int = Random.nextInt()
            val valueCollector = mutableListOf<Pair<Int, Int>>()
        }) exercise {

            fun testThatSendsContextToTeardown() = setup(object {
                val value = expectedValue
            }) exercise {
                expectedResult
            } verifyAnd {
            } teardown { result ->
                valueCollector.add(value to result)
            }

            testThatSendsContextToTeardown()
        } verify {
            assertEquals(expectedValue to expectedResult, valueCollector[0])
        }

        @Test
        fun whenFailureOccursInVerifyAndExceptionOccursInTeardownBothAreReported() = setup(object {
            val verifyFailure = AssertionError("Got 'em")
            val teardownException = Exception("Oh man, not good.")

            fun failingTestThatExplodesInTeardown() = setup(object {}) exercise {
            } verifyAnd { throw verifyFailure } teardown { throw teardownException }

        }) exercise {
            captureException { failingTestThatExplodesInTeardown() }
        } verify { result ->
            assertEquals(CompoundMintTestException(mapOf(
                    "Failure" to verifyFailure,
                    "Teardown exception" to teardownException)), result)
        }

        @Test
        fun whenExceptionOccursInSetupClosureWillNotRunExerciseOrTeardown() = setup(object {
            val setupException = Exception("Oh man, not good.")
            var exerciseOrVerifyTriggered = false

            fun testThatExplodeInSetupClosure() = setup(Unit) {
                throw setupException
            } exercise { exerciseOrVerifyTriggered = true } verify { exerciseOrVerifyTriggered = true }

        }) exercise {
            captureException { testThatExplodeInSetupClosure() }
        } verify { result ->
            assertEquals(setupException, result)
        }


        class TestTemplates {
            enum class Steps {
                BeforeAll, AfterAll, Setup, Exercise, Verify, Teardown
            }

            private val correctOrder = listOf(
                    Steps.BeforeAll,
                    Steps.Setup,
                    Steps.Exercise,
                    Steps.Verify,
                    Steps.Teardown,
                    Steps.AfterAll
            )

            @Test
            fun whenTestSucceedsIncludingTeardownSharedSetupAndSharedTeardownRunInCorrectOrder() = setup(object {
                val calls = mutableListOf<Steps>()
                fun beforeAll() = calls.add(Steps.BeforeAll).let { Unit }
                fun afterAll() = calls.add(Steps.AfterAll).let { Unit }
                val customSetup = testTemplate(sharedSetup = ::beforeAll, sharedTeardown = ::afterAll)

                fun testThatSucceeds() = customSetup(object {}) { calls.add(Steps.Setup) }
                        .exercise { calls.add(Steps.Exercise) }
                        .verifyAnd { calls.add(Steps.Verify) }
                        .teardown { calls.add(Steps.Teardown) }

            }) exercise {
                testThatSucceeds()
            } verify {
                assertEquals(correctOrder, calls)
            }

            @Test
            fun whenTestSucceedsEndingWithVerifySharedSetupAndSharedTeardownRunInCorrectOrder() = setup(object {
                val calls = mutableListOf<Steps>()
                fun beforeAll() = calls.add(Steps.BeforeAll).let { Unit }
                fun afterAll() = calls.add(Steps.AfterAll).let { Unit }
                val customSetup = testTemplate(sharedSetup = ::beforeAll, sharedTeardown = ::afterAll)

                fun testThatSucceeds() = customSetup(object {}) { calls.add(Steps.Setup) }
                        .exercise { calls.add(Steps.Exercise) }
                        .verify { calls.add(Steps.Verify) }

            }) exercise {
                testThatSucceeds()
            } verify {
                assertEquals(correctOrder - Steps.Teardown, calls)
            }

            @Test
            fun whenVerifyFailsSharedSetupAndSharedTeardownRunInCorrectOrder() = setup(object {
                val calls = mutableListOf<Steps>()
                fun beforeAll() = calls.add(Steps.BeforeAll).let { Unit }
                fun afterAll() = calls.add(Steps.AfterAll).let { Unit }
                val customSetup = testTemplate(sharedSetup = ::beforeAll, sharedTeardown = ::afterAll)

                fun testThatFails() = customSetup(object {}) { calls.add(Steps.Setup) }
                        .exercise { calls.add(Steps.Exercise) }
                        .verifyAnd { calls.add(Steps.Verify); fail("This test fails.") }
                        .teardown { calls.add(Steps.Teardown) }

            }) exercise {
                captureException { testThatFails() }
            } verify {
                assertEquals(correctOrder, calls)
            }

            @Test
            fun whenExceptionOccursInTeardownAndInTemplateTeardownBothAreReported() = setup(object {
                val teardownException = Exception("Oh man, not good.")
                val templateTeardownException = Exception("Now we're really off-road")
                val customSetup = testTemplate(sharedSetup = {}, sharedTeardown = { throw templateTeardownException })

                fun failingTestThatExplodesInTeardown() = customSetup(object {}) exercise {} verifyAnd {
                } teardown { throw teardownException }

            }) exercise {
                captureException { failingTestThatExplodesInTeardown() }
            } verify { result ->
                val expected = CompoundMintTestException(mapOf(
                        "Teardown exception" to teardownException,
                        "Template teardown exception" to templateTeardownException)
                )
                assertEquals(expected, result)
            }

            @Test
            fun testTemplateCanBeExtendedByCallingTestTemplateAgain() = setup(object {
                val calls = mutableListOf<String>()
                val customSetup = testTemplate(
                        sharedSetup = { calls.add("setup") }, sharedTeardown = { calls.add("teardown") }
                )

                val bolsteredCustomSetup = customSetup.extend(
                        sharedSetup = { calls.add("inner setup") }, sharedTeardown = { calls.add("inner teardown") }
                )

                fun test() = bolsteredCustomSetup(object {}) exercise {} verify {}
            }) exercise {
                test()
            } verify {
                assertEquals(listOf("setup", "inner setup", "inner teardown", "teardown"), calls)
            }

            @Test
            fun sharedSetupCanReturnContextThatWillBeProvidedToTheTeardown() = setup(object {
                val int = Random.nextInt()
                val callArguments = mutableListOf<Any>()
                val customSetup = testTemplate(
                        sharedSetup = { int },
                        sharedTeardown = { it: Int -> callArguments.add(it) }
                )

                fun testThatSucceeds() = customSetup(object {}) { }
                        .exercise { }
                        .verify { }

            }) exercise {
                testThatSucceeds()
            } verify {
                assertEquals(listOf<Any>(int), callArguments)
            }

        }

        class ReporterFeatures {

            enum class Call {
                ExerciseStart, ExerciseFinish, VerifyStart, VerifyFinish, TeardownStart, TeardownFinish
            }

            @Test
            fun willReportTestEventInOrderToReporter() = setup(object : StandardMintDispatcher {
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

            }) exercise {
                fun simpleTest() = setup(object {}) exercise {} verifyAnd {} teardown {}

                simpleTest()
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
            fun exerciseStartWillLogContext() = setup(object : StandardMintDispatcher {
                override val reporter = object : MintReporter {
                    var exerciseStartPayload: Any? = null
                    override fun exerciseStart(context: Any) {
                        exerciseStartPayload = context
                    }
                }

                val expectedObject = object {}

            }) exercise {
                fun simpleTest() = setup(expectedObject) exercise { } verify {}

                simpleTest()
            } verify {
                assertEquals(expectedObject, reporter.exerciseStartPayload)
            }

            @Test
            fun verifyStartWillLogThePayload() = setup(object : StandardMintDispatcher {
                override val reporter = object : MintReporter {
                    var verifyStartPayload: Any? = null
                    override fun verifyStart(payload: Any?) {
                        verifyStartPayload = payload
                    }
                }
                val expectedObject = object {}
            }) exercise {
                fun simpleTest() = setup(object {}) exercise { expectedObject } verify {}

                simpleTest()
            } verify {
                assertEquals(expectedObject, reporter.verifyStartPayload)
            }
        }

    }
}

