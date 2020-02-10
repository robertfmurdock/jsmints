package com.zegreatrob.minassert

import com.zegreatrob.mindiff.stringDiff
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

fun <T> T?.assertIsEqualTo(expected: T, message: String? = null) = assertEquals(expected, this, message.appendDiff(expected, this))

private fun <T> String?.appendDiff(expected: T, t1: T?): String {
    return "${this ?: ""}\n${stringDiff(expected.toString(), t1.toString())}\n"
}

fun <T> T?.assertIsNotEqualTo(expected: T, message: String? = null) = assertNotEquals(expected, this, message)

fun <T> List<T>.assertContains(item: T) = contains(item)
        .assertIsEqualTo(true, "${this.map { "$item" }} did not contain $item")
        .let { this }