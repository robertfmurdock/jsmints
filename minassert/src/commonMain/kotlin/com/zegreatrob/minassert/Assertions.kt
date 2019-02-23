package com.zegreatrob.minassert

import kotlin.test.assertEquals

fun <T> T?.assertIsEqualTo(expected: T, message: String? = null) = assertEquals(expected, this, message)

fun <T> MutableList<T>.assertContains(item: T) = contains(item)
        .assertIsEqualTo(true, "${this.map { "$item" }} did not contain $item")
        .let { this }