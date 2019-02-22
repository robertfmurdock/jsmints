package com.zegreatrob.testmints

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TestMintsTest {

    @Test
    fun verifyShouldThrowErrorWhenFailureOccurs() {
        val exercisedContext = setup(object {
        }) exercise {
        }

        try {
            exercisedContext verify { fail("LOL") }
        } catch (expectedFailure: AssertionError) {
            assertEquals("LOL", expectedFailure.message)
        }
    }

    @Test
    fun exerciseShouldHaveAccessToScopeOfSetupObject() {
        val expectedValue: Int? = Random.nextInt()
        var actualValue: Int? = null
        setup(object {
            @Suppress("UnnecessaryVariable")
            val value = expectedValue
        }) exercise {
            actualValue = value
        }
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun verifyShouldReceiveTheResultOfExerciseAsParameter() {
        val expectedValue = Random.nextInt()
        var actualValue: Int? = null
        setup(object {
        }) exercise {
            expectedValue
        } verify { result ->
            actualValue = result
        }
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun verifyShouldHaveAccessToScopeOfSetupObject() {
        val expectedValue: Int? = Random.nextInt()
        var actualValue: Int? = null
        setup(object {
            @Suppress("UnnecessaryVariable")
            val value = expectedValue
        }) exercise {
        } verify {
            actualValue = value
        }
        assertEquals(expectedValue, actualValue)
    }

}