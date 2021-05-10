package com.zegreatrob.minjson

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.invoke
import com.zegreatrob.testmints.setup
import kotlin.js.json
import kotlin.random.Random
import kotlin.test.Test

class JsonExtTest {

    @Test
    fun canUseJsonPathsOnJsonForTopLevelNodes() = setup(object {
        val expectedValue = "${Random.nextInt()}"
        val key = "key${Random.nextInt(10)}"
        val json = json(key to expectedValue)
    }) exercise {
        json.at<String>("/$key")
    } verify { result: String? ->
        result.assertIsEqualTo(expectedValue)
    }

    @Test
    fun canUseJsonPathsOnJsonForNextedNodes() = setup(object {
        val expectedValue = "${Random.nextInt()}"
        val key1 = "key${Random.nextInt(10)}"
        val key2 = "key${Random.nextInt(10)}"
        val key3 = "key${Random.nextInt(10)}"
        val json = json(key1 to json(key2 to json(key3 to expectedValue)))
    }) exercise {
        json.at<String>("/$key1/$key2/$key3")
    } verify { result: String? ->
        result.assertIsEqualTo(expectedValue)
    }

    @Test
    fun canUseJsonPathsOnJsonForArrayNodes() = setup(object {
        val expectedValue = "${Random.nextInt()}"
        val key1 = "key${Random.nextInt(10)}"
        val json = json(key1 to arrayOf(null, 7, expectedValue, 99))
    }) exercise {
        json.at<String>("/$key1/2")
    } verify { result: String? ->
        result.assertIsEqualTo(expectedValue)
    }

    @Test
    fun whenValueIsNotFoundWillReturnNull() = setup(object {
        val json = json("key" to "expectedValue")
    }) exercise {
        json.at<String>("/totallyWrongPath")
    } verify { result: String? ->
        result.assertIsEqualTo(null)
    }

    @Test
    fun whenValueIsNotFoundNestedWillReturnNull() = setup(object {
        val json = json("key" to json("key2" to json("nope" to "expectedValue")))
    }) exercise {
        json.at<String>("/key/totallyWrongPath/nope")
    } verify { result: String? ->
        result.assertIsEqualTo(null)
    }

}