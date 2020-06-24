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
}