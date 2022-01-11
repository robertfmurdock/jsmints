package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.MintReporter
import io.kotest.core.spec.style.FunSpec
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.fail

fun FunSpec.test(testName: String, test: () -> Unit) {
    test(testName) { test() }
}

@Suppress("unused")
class ExampleUsage : FunSpec({
    fun Int.plusOne() = this + 1

    test("simpleCase", fun() = setup(object {
        val input: Int = Random.nextInt()
        val expected = input + 1
    }) exercise {
        input.plusOne()
    } verify { result ->
        assertEquals(expected, result)
    })
})

@Suppress("unused")
class Features : FunSpec({

    test("verifyShouldThrowErrorWhenFailureOccurs", fun() = setup(object {

        fun simulatedTestThatFailsInVerify() = setup() exercise {} verify { fail("LOL") }

    }) exercise {
        captureException { simulatedTestThatFailsInVerify() }
    } verify { result ->
        assertEquals("LOL", result?.message)
    })

    test("exerciseShouldHaveAccessToScopeOfSetupObject", fun() = setup(object {
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
    })

    test("verifyShouldReceiveTheResultOfExerciseAsParameter", fun() = setup(object {
        val expectedValue = Random.nextInt()
        var actualValue: Int? = null

        fun testThatPassesResultToVerify() = setup() exercise { expectedValue } verify { result ->
            actualValue = result
        }

    }) exercise {
        testThatPassesResultToVerify()
    } verify {
        assertEquals(expectedValue, actualValue)
    })

    test("verifyShouldHaveAccessToSetupContext", fun() = setup(object {
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
    })

    test("tearDownShouldHaveAccessToScopeOfSetupObjectAndResult", fun() = setup(object {
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
    })

    test("whenFailureOccursInVerifyAndExceptionOccursInTeardownBothAreReported", fun() = setup(object {
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
    })

    test("whenExceptionOccursInSetupClosureWillNotRunExerciseOrTeardown", fun() = setup(object {
        val setupException = Exception("Oh man, not good.")
        var exerciseOrVerifyTriggered = false

        fun testThatExplodeInSetupClosure() = setup {
            throw setupException
        } exercise { exerciseOrVerifyTriggered = true } verify { exerciseOrVerifyTriggered = true }

    }) exercise {
        captureException { testThatExplodeInSetupClosure() }
    } verify { result ->
        assertEquals(setupException, result)
    })

})

enum class Steps {
    TemplateSetup, TemplateTeardown, Setup, Exercise, Verify, Teardown
}

@Suppress("unused")
class TestTemplates : FunSpec({

    val correctOrder = listOf(
        Steps.TemplateSetup,
        Steps.Setup,
        Steps.Exercise,
        Steps.Verify,
        Steps.Teardown,
        Steps.TemplateTeardown
    )

    fun List<() -> Any?>.runSuite() = forEach { it() }

    test("whenTestSucceedsIncludingTeardownSharedSetupAndSharedTeardownRunInCorrectOrder", fun() = setup(object {
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
    })

    test("whenTestSucceedsEndingWithVerifySharedSetupAndSharedTeardownRunInCorrectOrder", fun() = setup(object {
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
    })

    test("wrapperFunctionCanBeUsedAsAlternativeToSharedSetupAndSharedTeardown", fun() = setup(object {
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
    })

    test("wrapperFunctionProvideSharedContextAlso", fun() = setup(object {
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
    })

    test("sharedContextCanBeUsedAsContext", fun() = setup(object {
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
    })

    test("canExtendToTransformSharedContextUsingSharedSetupAndTeardown", fun() = setup(object {
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
    })

    test("canExtendToTransformSharedContextUsingWrapper", fun() = setup(object {
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
    })

    test("whenWrapperFunctionDoesNotCallTheTestTheTestWillFail", fun() = setup(object {
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
    })

    test("whenWrapperFunctionDoesNotCallTheTestTheTestWillFailIncludingTeardown", fun() = setup(object {
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
    })

    test("whenVerifyFailsSharedSetupAndSharedTeardownRunInCorrectOrder", fun() = setup(object {
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
    })

    test("whenExceptionOccursInTeardownAndInTemplateTeardownBothAreReported", fun() = setup(object {
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
    })

    test("testTemplateCanBeExtended", fun() = setup(object {
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
    })

    test("templateCanBeExtendedWithBeforeAllFunctionThatWillOnlyRunOnceForAllAttachedTests", fun() = setup(object {
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
    })

    test("sharedSetupCanReturnContextThatWillBeProvidedToTheTeardown", fun() = setup(object {
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
    })

    test("templateCanBeBuiltWithBeforeAllFunctionThatWillOnlyRunOnceForAllAttachedTests", fun() = setup(object {
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
    })

    test("templateWithBeforeAllWillNotPerformBeforeAllWhenThereAreNoTests", fun() = setup(object {
        var beforeAllCount = 0
        val testSuite: List<() -> Unit> = emptyList()
    }) {
        testTemplate(beforeAll = { beforeAllCount++ })
    } exercise {
        testSuite.runSuite()
    } verify {
        assertEquals(0, beforeAllCount)
    })

    test("templateExtendedByBeforeAllCanMergeSharedContextEasily", fun() = setup(object {
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
    })

})


enum class Call {
    ExerciseStart, ExerciseFinish, VerifyStart, VerifyFinish, TeardownStart, TeardownFinish
}

@Suppress("unused")
class ReporterFeatures : FunSpec({

    test("willReportTestEventInOrderToReporter", fun() = setup(object : StandardMintDispatcher {
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
    })

    test("reporterCanBeConfiguredAfterTemplatesAreDefined", fun() = setup(object : StandardMintDispatcher {
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
    })

    test("exerciseStartWillLogContext", fun() = setup(object : StandardMintDispatcher {
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
    })

    test("verifyStartWillLogThePayload", fun() = setup(object : StandardMintDispatcher {
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
    })

    test("verifyFinishWillWaitUntilVerifyIsComplete", fun() = setup(object : StandardMintDispatcher {
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
    })
})