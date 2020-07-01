package com.zegreatrob.minspy

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class SpyTest {

    @Test
    fun givenNullableOptionsWillCorrectlyReturnThem() = setup(object {
        val spy = SpyData<String, Int?>().apply {
            whenever("1", 1)
            whenever("Nah", null)
        }
    }) exercise {
        spy.spyFunction("Nah")
    } verify { result ->
        result.assertIsEqualTo(null)
    }

    @Test
    fun hasEasyCallFunctionForWhenThereIsNoInput() = setup(object {
        val spy = SpyData<Unit, Int>().apply { whenever(Unit, 1) }
    }) exercise {
        spy.spyFunction()
    } verify { result ->
        result.assertIsEqualTo(1)
    }

    @Test
    fun doesNotRequireSetupWhenThereIsNoReturn() = setup(object {
        val spy = SpyData<Int, Unit>()
    }) exercise {
        spy.spyFunction(77)
    } verify {
        spy.spyReceivedValues
                .assertIsEqualTo(listOf(77))
    }

    @Test
    fun whenNoArgsNoReturnDoesNotRequireSetup() = setup(object {
        val spy = SpyData<Unit, Unit>()
        val timesToCall = 3
    }) exercise {
        repeat(timesToCall) { spy.spyFunction() }
    } verify {
        spy.callCount.assertIsEqualTo(3)
    }

}
