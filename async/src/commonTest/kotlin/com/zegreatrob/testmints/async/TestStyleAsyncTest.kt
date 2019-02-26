package com.zegreatrob.testmints.async

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

@Suppress("unused")
class TestStyleAsyncTest {

    class ExampleUsage {

        private fun Int.plusOne() = this + 1

        @Test
        fun simpleCase() = testAsync {
            setupAsync(object {
                val input: Int = Random.nextInt()
                val expected = input + 1
            }) exerciseAsync {
                input.plusOne()
            } verifyAsync { result ->
                assertEquals(expected, result)
            }
        }

        @Test
        fun caseWithAsyncInsideTheSetupClosure() = testAsync {
            setupAsync(object {
                val input: Int = Random.nextInt()
                val expected = input + 1
                var databaseSetupCounter = 0
            }) {
                withContext(Dispatchers.Default) {
                    delay(4)
                    databaseSetupCounter++
                }
            } exerciseAsync {
                input + databaseSetupCounter
            } verifyAsync { result ->
                assertEquals(expected, result)
            }
        }

    }

    class Features {
        @Test
        fun canFailAsync() {
            try {
                testAsync {
                    val exercisedContext = setupAsync(object {
                    }) exerciseAsync {
                    }

                    exercisedContext verifyAsync { fail("LOL") }
                }
            } catch (expectedFailure: AssertionError) {
                assertEquals("LOL", expectedFailure.message)
            }
        }

        @Test
        fun canFailAsyncWithCoroutine() {
            try {
                testAsync {
                    val exercisedContext = setupAsync(object {
                    }) exerciseAsync {
                    }

                    exercisedContext verifyAsync {
                        withContext(Dispatchers.Default) {
                            delay(3)
                            fail("LOL")
                        }
                    }
                }
            } catch (expectedFailure: AssertionError) {
                assertEquals("LOL", expectedFailure.message)
            }
        }

        @Test
        fun verifyShouldThrowErrorWhenFailureOccurs() {
            try {
                testAsync {
                    val exercisedContext = setupAsync(object {
                    }) exerciseAsync {
                    }
                    exercisedContext verifyAsync { fail("LOL") }
                }
            } catch (expectedFailure: AssertionError) {
                assertEquals("LOL", expectedFailure.message)
            }
        }

        @Test
        fun exerciseShouldHaveAccessToScopeOfSetupObject() {
            val expectedValue: Int? = Random.nextInt()
            var actualValue: Int? = null
            testAsync {
                setupAsync(object {
                    @Suppress("UnnecessaryVariable")
                    val value = expectedValue
                }) exerciseAsync {
                    actualValue = value
                }
            }
            assertEquals(expectedValue, actualValue)
        }

        @Test
        fun verifyShouldReceiveTheResultOfExerciseAsParameter() {
            val expectedValue = Random.nextInt()
            var actualValue: Int? = null
            testAsync {
                setupAsync(object {
                }) exerciseAsync {
                    expectedValue
                } verifyAsync { result ->
                    actualValue = result
                }
            }
            assertEquals(expectedValue, actualValue)
        }

        @Test
        fun verifyShouldHaveAccessToScopeOfSetupObject() {
            val expectedValue: Int? = Random.nextInt()
            var actualValue: Int? = null
            testAsync {
                setupAsync(object {
                    @Suppress("UnnecessaryVariable")
                    val value = expectedValue
                }) exerciseAsync {
                } verifyAsync {
                    actualValue = value
                }
            }
            assertEquals(expectedValue, actualValue)
        }
    }

}