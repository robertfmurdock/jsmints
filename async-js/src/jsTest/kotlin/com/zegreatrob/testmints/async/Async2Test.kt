package com.zegreatrob.testmints.async

import kotlinx.coroutines.*
import kotlin.js.Promise
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
        val assertionError = AssertionError("ExpectedAssertion")
    }) exercise {
        try {
            testThatFailsDuringVerify(assertionError).await()
            fail("The test should fail.")
        } catch (bad: Throwable) {
            bad
        }
    } verify { result: Throwable ->
        assertEquals(result, assertionError)
    }

    private fun testThatFailsDuringVerify(assertionError: AssertionError): Promise<Unit> = (setupAsync2(object {
    }) exercise {
    } verify {
        throw assertionError
    }).unsafeCast<Promise<Unit>>()

    @Test
    fun willFailExerciseCorrectly() = setupAsync2(object {
        val assertionError = AssertionError("ExpectedAssertion")
    }) exercise {
        try {
            testThatFailsDuringExercise(assertionError).await()
            fail("The test should fail.")
        } catch (bad: Throwable) {
            bad
        }
    } verify { result: Throwable ->
        assertEquals(result, assertionError)
    }

    private fun testThatFailsDuringExercise(assertionError: AssertionError): Promise<Unit> = (setupAsync2(object {
    }) exercise {
        throw assertionError
    } verify {
    }).unsafeCast<Promise<Unit>>()

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
    fun canProvideScopeUsingScopeMintSetupScopeWillCompleteBeforeExercise() = setupAsync2(object : ScopeMint() {
        val expectedValue = Random.nextInt()
        val asyncProducedValue = setupScope.async { delay(40); expectedValue }
    }) exercise {
        asyncProducedValue.isCompleted
    } verify { setupAsyncCompletedBeforeExercise ->
        assertEquals(true, setupAsyncCompletedBeforeExercise)
        assertEquals(expectedValue, asyncProducedValue.await())
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

}