package com.zegreatrob.mindiff

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import com.zegreatrob.testmints.invoke
import kotlin.test.Test

class DiffTest {

    @Test
    fun givenSingleCharacterStringsThatAreTheSameWillDeclareNoDiff() = setup(object {
        val l = "1"
        val r = "1"
    }) exercise {
        diff(l, r)
    } verify { result ->
        result.assertIsEqualTo(".")
    }

    @Test
    fun givenSingleCharacterStringsThatAreDifferentWillDeclareDifferent() = setup(object {
        val l = "1"
        val r = "0"
    }) exercise {
        diff(l, r)
    } verify { result ->
        result.assertIsEqualTo("x")
    }

    @Test
    fun givenManyCharactersWillShowMatchesAndDifferences() = setup(object {
        val l = "I do that thing"
        val r = "I do the thing"
    }) exercise {
        diff(l, r)
    } verify { result ->
        result.assertIsEqualTo(".......xxxxxxxx")
    }

    @Test
    fun givenManyCharactersMixedAndMatched() = setup(object {
        val l = "I do that thing"
        val r = "U dO Thot thang please"
    }) exercise {
        diff(l, r)
    } verify { result ->
        result.assertIsEqualTo("x..x.x.x....x..xxxxxxx")
    }
}
