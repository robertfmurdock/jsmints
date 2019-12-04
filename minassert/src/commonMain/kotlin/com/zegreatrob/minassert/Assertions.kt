package com.zegreatrob.minassert

import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

fun <T> T?.assertIsEqualTo(expected: T, message: String? = null) = assertEquals(expected, this, message)

fun <T> T?.assertIsNotEqualTo(expected: T, message: String? = null) = assertNotEquals(expected, this, message)

fun <T> List<T>.assertContains(item: T) = contains(item)
        .assertIsEqualTo(true, "${this.map { "$item" }} did not contain $item")
        .let { this }