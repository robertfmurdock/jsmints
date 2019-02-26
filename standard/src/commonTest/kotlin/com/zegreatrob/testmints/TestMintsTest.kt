package com.zegreatrob.testmints

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
        fun verifyShouldThrowErrorWhenFailureOccurs() {
            try {
                simulatedTestThatFailsInVerify()
                fail("This line should never be hit, because testmints should report when verify has a failure.")
            } catch (expectedFailure: AssertionError) {
                assertEquals("LOL", expectedFailure.message)
            }
        }

        private fun simulatedTestThatFailsInVerify(): Unit = setup<Any>(object {
        }) exercise {
        } verify { fail("LOL") }

        class ValueCollector(var actualValue: Int? = null)

        @Test
        fun exerciseShouldHaveAccessToScopeOfSetupObject() {
            val expectedValue: Int? = Random.nextInt()
            val valueCollector = ValueCollector()
            valueCollector.simulateTestAndCollectsSetupValueDuringExercise(expectedValue)
            assertEquals(expectedValue, valueCollector.actualValue)
        }

        private fun ValueCollector.simulateTestAndCollectsSetupValueDuringExercise(expectedValue: Int?) = setup(object {
            @Suppress("UnnecessaryVariable")
            val value = expectedValue
        }) exercise {
            actualValue = value
        } verify {
        }

        @Test
        fun verifyShouldReceiveTheResultOfExerciseAsParameter() {
            val expectedValue = Random.nextInt()
            val valueCollector = ValueCollector()
            valueCollector.simulateTestAndCollectResultValueDuringVerify(expectedValue)
            assertEquals(expectedValue, valueCollector.actualValue)
        }

        private fun ValueCollector.simulateTestAndCollectResultValueDuringVerify(expectedValue: Int) = setup(object {
        }) exercise {
            expectedValue
        } verify { result ->
            actualValue = result
        }

        @Test
        fun verifyShouldHaveAccessToScopeOfSetupObject() {
            val expectedValue: Int? = Random.nextInt()
            val valueCollector = ValueCollector()
            valueCollector.simulateTestAndCollectSetupValueDuringVerify(expectedValue)
            assertEquals(expectedValue, valueCollector.actualValue)
        }

        private fun ValueCollector.simulateTestAndCollectSetupValueDuringVerify(expectedValue: Int?) = setup(object {
            @Suppress("UnnecessaryVariable")
            val value = expectedValue
        }) exercise {
        } verify {
            actualValue = value
        }

    }
}