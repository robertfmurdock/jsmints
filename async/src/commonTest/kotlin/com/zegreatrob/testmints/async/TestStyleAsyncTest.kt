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
        fun simpleCase() = asyncSetup(object {
            val input: Int = Random.nextInt()
            val expected = input + 1
        }) exercise {
            input.plusOne()
        } verify { result ->
            assertEquals(expected, result)
        }

        @Test
        fun caseWithAsyncInsideTheSetupClosure() = asyncSetup(object {
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
        fun canFailAsync() = testAsync {
            try {
                waitForTest {
                    asyncSetup<Any>(object {
                    }) exercise {
                    } verify { fail("LOL") }
                }
            } catch (expectedFailure: AssertionError) {
                assertEquals("LOL", expectedFailure.message)
            }
        }

        @Test
        fun canFailAsyncWithCoroutine() = testAsync {
            try {
                waitForTest {
                    asyncSetup<Any>(object {
                    }) exercise {
                    } verify {
                        withContext<Nothing>(Dispatchers.Default) {
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
        fun verifyShouldThrowErrorWhenFailureOccurs() = testAsync {
            try {
                waitForTest {
                    asyncSetup<Any>(object {
                    }) exercise {
                    } verify { fail("LOL") }
                }
            } catch (expectedFailure: AssertionError) {
                assertEquals("LOL", expectedFailure.message)
            }
        }

        @Test
        fun exerciseShouldHaveAccessToScopeOfSetupObject() = testAsync {
            val expectedValue: Int? = Random.nextInt()
            var actualValue: Int? = null
            waitForTest {
                asyncSetup(object {
                    @Suppress("UnnecessaryVariable")
                    val value = expectedValue
                }) exercise {
                    actualValue = value
                } verify {}
            }

            assertEquals(expectedValue, actualValue)
        }

        @Test
        fun verifyShouldReceiveTheResultOfExerciseAsParameter() = testAsync {
            val expectedValue = Random.nextInt()
            var actualValue: Int? = null
            waitForTest {
                testAsync {
                    setupAsync(object {
                    }) exerciseAsync {
                        expectedValue
                    } verifyAsync { result ->
                        actualValue = result
                    }
                }
            }
            assertEquals(expectedValue, actualValue)
        }

        @Test
        fun verifyShouldHaveAccessToScopeOfSetupObject() = testAsync {
            val expectedValue: Int? = Random.nextInt()
            var actualValue: Int? = null
            waitForTest {
                asyncSetup(object {
                    @Suppress("UnnecessaryVariable")
                    val value = expectedValue
                }) exercise {
                } verify {
                    actualValue = value
                }
            }
            assertEquals(expectedValue, actualValue)
        }

    }

}