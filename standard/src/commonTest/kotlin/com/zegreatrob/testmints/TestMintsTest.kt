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

            fun simulatedTestThatFailsInVerify() = setup() exercise {} verify { fail("LOL") }

        }) exercise {
            captureException { simulatedTestThatFailsInVerify() }
        } verify { result ->
            assertEquals("LOL", result?.message)
        }

        @Test
        fun exerciseShouldHaveAccessToScopeOfSetupObject() = setup(object {
            val expectedValue: Int = Random.nextInt()
            var actualValue: Int? = null

            fun testThatUsesContextInExercise() = setup(object {
                val value = expectedValue
            }) exercise {
                actualValue = value
            } verify {
            }

        }) exercise {
            testThatUsesContextInExercise()
        } verify {
            assertEquals(expectedValue, actualValue)
        }

        @Test
        fun verifyShouldReceiveTheResultOfExerciseAsParameter() = setup(object {
            val expectedValue = Random.nextInt()
            var actualValue: Int? = null

            fun testThatPassesResultToVerify() = setup() exercise { expectedValue } verify { result ->
                actualValue = result
            }

        }) exercise {
            testThatPassesResultToVerify()
        } verify {
            assertEquals(expectedValue, actualValue)
        }

        @Test
        fun verifyShouldHaveAccessToSetupContext() = setup(object {
            val expectedValue: Int = Random.nextInt()
            var actualValue: Int? = null

            fun testThatUsesContextInVerify() = setup(object {
                val value = expectedValue
            }) exercise {} verify {
                actualValue = value
            }

        }) exercise {
            testThatUsesContextInVerify()
        } verify {
            assertEquals(expectedValue, actualValue)
        }

        @Test
        fun tearDownShouldHaveAccessToScopeOfSetupObjectAndResult() = setup(object {
            val expectedValue: Int = Random.nextInt()
            val expectedResult: Int = Random.nextInt()
            var teardownReceived: Pair<Int, Int>? = null
        }) exercise {

            fun testThatSendsContextToTeardown() = setup(object {
                val value = expectedValue
            }) exercise {
                expectedResult
            } verifyAnd {
            } teardown { result ->
                teardownReceived = value to result
            }

            testThatSendsContextToTeardown()
        } verify {
            assertEquals(expectedValue to expectedResult, teardownReceived)
        }

        @Test
        fun whenFailureOccursInVerifyAndExceptionOccursInTeardownBothAreReported() = setup(object {
            val verifyFailure = AssertionError("Got 'em")
            val teardownException = Exception("Oh man, not good.")

            fun failingTestThatExplodesInTeardown() = setup() exercise {
            } verifyAnd { throw verifyFailure } teardown { throw teardownException }

        }) exercise {
            captureException { failingTestThatExplodesInTeardown() }
        } verify { result ->
            assertEquals(
                CompoundMintTestException(
                    mapOf(
                        "Failure" to verifyFailure,
                        "Teardown exception" to teardownException
                    )
                ), result
            )
        }

        @Test
        fun whenExceptionOccursInSetupClosureWillNotRunExerciseOrTeardown() = setup(object {
            val setupException = Exception("Oh man, not good.")
            var exerciseOrVerifyTriggered = false

            fun testThatExplodeInSetupClosure() = setup {
                throw setupException
            } exercise { exerciseOrVerifyTriggered = true } verify { exerciseOrVerifyTriggered = true }

        }) exercise {
            captureException { testThatExplodeInSetupClosure() }
        } verify { result ->
            assertEquals(setupException, result)
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
            fun whenTestSucceedsIncludingTeardownSharedSetupAndSharedTeardownRunInCorrectOrder() = setup(object {
                val calls = mutableListOf<Steps>()
                val customSetup = testTemplate(
                    sharedSetup = { calls.add(Steps.TemplateSetup) },
                    sharedTeardown = { calls.add(Steps.TemplateTeardown) }
                )

                fun testThatSucceeds() = customSetup { calls.add(Steps.Setup) }
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
                val customSetup = testTemplate(
                    sharedSetup = { calls.add(Steps.TemplateSetup) },
                    sharedTeardown = { calls.add(Steps.TemplateTeardown) }
                )

                fun testThatSucceeds() = customSetup { calls.add(Steps.Setup) }
                    .exercise { calls.add(Steps.Exercise) }
                    .verify { calls.add(Steps.Verify) }

            }) exercise {
                testThatSucceeds()
            } verify {
                assertEquals(correctOrder - Steps.Teardown, calls)
            }

            @Test
            fun wrapperFunctionCanBeUsedAsAlternativeToSharedSetupAndSharedTeardown() = setup(object {
                val calls = mutableListOf<Steps>()
                val customSetup = testTemplate(wrapper = { runTest: () -> Unit ->
                    calls.add(Steps.TemplateSetup)
                    runTest()
                    calls.add(Steps.TemplateTeardown)
                })

                fun testThatSucceeds() = customSetup { calls.add(Steps.Setup) }
                    .exercise { calls.add(Steps.Exercise) }
                    .verify { calls.add(Steps.Verify) }

            }) exercise {
                testThatSucceeds()
            } verify {
                assertEquals(correctOrder - Steps.Teardown, calls)
            }

            @Test
            fun wrapperFunctionProvideSharedContextAlso() = setup(object {
                val calls = mutableListOf<Steps>()

                val expectedSharedContext = 17
                val customSetup = testTemplate(wrapper = { runTest: (sharedContext: Int) -> Unit ->
                    calls.add(Steps.TemplateSetup)
                    runTest(expectedSharedContext)
                    calls.add(Steps.TemplateTeardown)
                })

                var sharedContextReceived = 0

                fun testThatSucceeds() = customSetup(contextProvider = { sc -> sharedContextReceived = sc }) {
                    calls.add(Steps.Setup)
                } exercise { calls.add(Steps.Exercise) } verify { calls.add(Steps.Verify) }

            }) exercise {
                testThatSucceeds()
            } verify {
                assertEquals(correctOrder - Steps.Teardown, calls)
                assertEquals(expectedSharedContext, sharedContextReceived)
            }

            @Test
            fun sharedContextCanBeUsedAsContext() = setup(object {
                val sharedContext = 47
                val customSetup = testTemplate(sharedSetup = { sharedContext })
                var contextReceived: Any? = null
                var additionalActionsCallCount = 0
                fun simpleTestUsingOnlySharedContext() = customSetup {
                    additionalActionsCallCount++
                } exercise {
                    contextReceived = this
                } verify { }
            }) exercise {
                simpleTestUsingOnlySharedContext()
            } verify {
                assertEquals(sharedContext, contextReceived)
                assertEquals(1, additionalActionsCallCount)
            }

            @Test
            fun canExtendToTransformSharedContextUsingSharedSetupAndTeardown() = setup(object {
                val calls = mutableListOf<Steps>()

                val originalSharedContext = 41

                val extendedSetup = testTemplate(sharedSetup = { originalSharedContext })
                    .extend(
                        sharedSetup = { sc ->
                            calls.add(Steps.TemplateSetup)
                            "$sc bottles of beer on the wall."
                        },
                        sharedTeardown = { calls.add(Steps.TemplateTeardown) }
                    )

                var sharedContextReceived: Any? = null

                fun testThatSucceeds() = extendedSetup(contextProvider = { sc -> sharedContextReceived = sc }) {
                    calls.add(Steps.Setup)
                }.exercise { calls.add(Steps.Exercise) }
                    .verifyAnd { calls.add(Steps.Verify) }
                    .teardown { calls.add(Steps.Teardown) }

            }) exercise {
                testThatSucceeds()
            } verify {
                assertEquals(correctOrder, calls)
                assertEquals("$originalSharedContext bottles of beer on the wall.", sharedContextReceived)
            }

            @Test
            fun canExtendToTransformSharedContextUsingWrapper() = setup(object {
                val calls = mutableListOf<Steps>()

                val originalSharedContext = 67

                val extendedSetup = testTemplate(sharedSetup = { originalSharedContext })
                    .extend<String>(wrapper = { sc, test ->
                        calls.add(Steps.TemplateSetup)
                        test("$sc bottles of beer on the wall.")
                        calls.add(Steps.TemplateTeardown)
                    })

                var sharedContextReceived: Any? = null

                fun testThatSucceeds() = extendedSetup(contextProvider = { sc -> sharedContextReceived = sc }) {
                    calls.add(Steps.Setup)
                }.exercise { calls.add(Steps.Exercise) }
                    .verifyAnd { calls.add(Steps.Verify) }
                    .teardown { calls.add(Steps.Teardown) }

            }) exercise {
                testThatSucceeds()
            } verify {
                assertEquals(correctOrder, calls)
                assertEquals("$originalSharedContext bottles of beer on the wall.", sharedContextReceived)
            }

            @Test
            fun whenWrapperFunctionDoesNotCallTheTestTheTestWillFail() = setup(object {
                val customSetup = testTemplate(wrapper = {})

                fun testThatFailsBecauseOfBadTemplate() = customSetup()
                    .exercise { }
                    .verify { }

            }) exercise {
                captureException { testThatFailsBecauseOfBadTemplate() }
            } verify { result ->
                assertEquals(
                    "Incomplete test template: the wrapper function never called the test function",
                    result?.message
                )
            }

            @Test
            fun whenWrapperFunctionDoesNotCallTheTestTheTestWillFailIncludingTeardown() = setup(object {
                val customSetup = testTemplate(wrapper = {})

                fun testThatFailsBecauseOfBadTemplate() = customSetup()
                    .exercise { }
                    .verifyAnd { }
                    .teardown { }

            }) exercise {
                captureException { testThatFailsBecauseOfBadTemplate() }
            } verify { result ->
                assertEquals(
                    "Incomplete test template: the wrapper function never called the test function",
                    result?.message
                )
            }

            @Test
            fun whenVerifyFailsSharedSetupAndSharedTeardownRunInCorrectOrder() = setup(object {
                val calls = mutableListOf<Steps>()
                fun beforeAll() = calls.add(Steps.TemplateSetup)
                fun afterAll() = calls.add(Steps.TemplateTeardown)
                val customSetup = testTemplate(sharedSetup = ::beforeAll, sharedTeardown = ::afterAll)

                fun testThatFails() = customSetup { calls.add(Steps.Setup) }
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

                fun failingTestThatExplodesInTeardown() = customSetup() exercise {} verifyAnd {
                } teardown { throw teardownException }

            }) exercise {
                captureException { failingTestThatExplodesInTeardown() }
            } verify { result ->
                val expected = CompoundMintTestException(
                    mapOf(
                        "Teardown exception" to teardownException,
                        "Template teardown exception" to templateTeardownException
                    )
                )
                assertEquals(expected, result)
            }

            @Test
            fun testTemplateCanBeExtended() = setup(object {
                val calls = mutableListOf<String>()
                val customSetup = testTemplate(
                    sharedSetup = { calls.add("setup") }, sharedTeardown = { calls.add("teardown") }
                )

                val bolsteredCustomSetup = customSetup.extend(
                    sharedSetup = { calls.add("inner setup") }, sharedTeardown = { calls.add("inner teardown") }
                )

                fun test() = bolsteredCustomSetup() exercise {} verify {}
            }) exercise {
                test()
            } verify {
                assertEquals(listOf("setup", "inner setup", "inner teardown", "teardown"), calls)
            }

            @Test
            fun templateCanBeExtendedWithBeforeAllFunctionThatWillOnlyRunOnceForAllAttachedTests() = setup(object {
                var calls = mutableListOf<String>()
                val customSetup = testTemplate(wrapper = {
                    calls.add("wrapSetup")
                    it()
                    calls.add("wrapTeardown")
                })
                    .extend(beforeAll = { calls.add("beforeAll") })
                val testSuite = (1..3).map {
                    fun() = customSetup { }
                        .exercise { }
                        .verify { }
                }
            }) exercise {
                testSuite.runSuite()
            } verify {
                assertEquals(
                    listOf(
                        "wrapSetup",
                        "beforeAll",
                        "wrapTeardown",
                        "wrapSetup",
                        "wrapTeardown",
                        "wrapSetup",
                        "wrapTeardown"
                    ), calls
                )
            }

            @Test
            fun sharedSetupCanReturnContextThatWillBeProvidedToTheTeardown() = setup(object {
                val int = Random.nextInt()
                val callArguments = mutableListOf<Any>()
                val customSetup = testTemplate(
                    sharedSetup = { int },
                    sharedTeardown = { it: Int -> callArguments.add(it) }
                )

                fun testThatSucceeds() = customSetup { }
                    .exercise { }
                    .verify { }

            }) exercise {
                testThatSucceeds()
            } verify {
                assertEquals(listOf<Any>(int), callArguments)
            }

            @Test
            fun templateCanBeBuiltWithBeforeAllFunctionThatWillOnlyRunOnceForAllAttachedTests() = setup(object {
                var beforeAllCount = 0
                val customSetup = testTemplate(beforeAll = { beforeAllCount++ })
                val testSuite = (1..3).map {
                    fun() = customSetup { }
                        .exercise { }
                        .verify { }
                }
            }) exercise {
                testSuite.runSuite()
            } verify {
                assertEquals(1, beforeAllCount)
            }

            @Test
            fun templateWithBeforeAllWillNotPerformBeforeAllWhenThereAreNoTests() = setup(object {
                var beforeAllCount = 0
                val customSetup = testTemplate(beforeAll = { beforeAllCount++ })
                val testSuite: List<() -> Unit> = emptyList()
            }) exercise {
                testSuite.runSuite()
            } verify {
                assertEquals(0, beforeAllCount)
            }

            @Test
            fun templateExtendedByBeforeAllCanMergeSharedContextEasily() = setup(object {
                val parentSharedContext = "parent shared context"
                val innerBeforeAllContext = 87

                val customSetup = testTemplate<String>(wrapper = { it(parentSharedContext) })
                    .extend(beforeAll = { innerBeforeAllContext }, mergeContext = { sc, bac ->
                        Pair(sc, bac)
                    })

                var capturedContext: Any? = null

                fun theCoolTest() = customSetup()
                    .exercise { capturedContext = this }
                    .verify { }
            }) exercise {
                theCoolTest()
            } verify {
                assertEquals(Pair(parentSharedContext, innerBeforeAllContext), capturedContext)
            }

            private fun List<() -> Any?>.runSuite() = forEach { it() }

        }

        class ReporterFeatures {

            enum class Call {
                ExerciseStart, ExerciseFinish, VerifyStart, VerifyFinish, TeardownStart, TeardownFinish
            }

            @Test
            fun willReportTestEventInOrderToReporter() = setup(object : StandardMintDispatcher {
                val calls = mutableListOf<Call>()
                private fun record(call: Call) = calls.add(call).let { }

                fun simpleTest() = setup() exercise {} verifyAnd {} teardown {}

                override val reporter = object : MintReporter {
                    override fun exerciseStart(context: Any) = record(Call.ExerciseStart)
                    override fun exerciseFinish() = record(Call.ExerciseFinish)
                    override fun verifyStart(payload: Any?) = record(Call.VerifyStart)
                    override fun verifyFinish() = record(Call.VerifyFinish)
                    override fun teardownStart() = record(Call.TeardownStart)
                    override fun teardownFinish() = record(Call.TeardownFinish)
                }

            }) exercise {
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
            fun reporterCanBeConfiguredAfterTemplatesAreDefined() = setup(object : StandardMintDispatcher {
                val templatedSetup = testTemplate(sharedSetup = {})
                fun simpleTest() = templatedSetup() exercise {} verifyAnd {} teardown {}
                var exerciseCalled = false
                override val reporter = object : MintReporter {
                    override fun exerciseStart(context: Any) {
                        exerciseCalled = true
                    }
                }
            }) exercise {
                simpleTest()
            } verify {
                assertEquals(true, exerciseCalled)
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

                fun simpleTest() = setup(expectedObject) exercise { } verify {}

            }) exercise {
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
                fun simpleTest() = setup() exercise { expectedObject } verify {}
            }) exercise {
                simpleTest()
            } verify {
                assertEquals(expectedObject, reporter.verifyStartPayload)
            }

            @Test
            fun verifyFinishWillWaitUntilVerifyIsComplete() = setup(object : StandardMintDispatcher {
                val verifyState = mutableListOf<String>()
                var result: String? = null
                override val reporter = object : MintReporter {
                    override fun verifyFinish() {
                        result = verifyState.joinToString("")
                    }
                }
                val expectedResult = object {}
                val expectedException = Exception("end in failure")

                fun simpleTest() = setup() exercise { expectedResult } verify {
                    verifyState.add("abc")
                    throw expectedException
                }

            }) exercise {
                captureException { simpleTest() }
            } verify { exception ->
                assertEquals(expectedException.message, exception?.message)
                assertEquals("abc", result)
            }
        }

    }
}
