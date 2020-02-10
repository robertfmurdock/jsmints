package com.zegreatrob.mindiff

import kotlin.math.max

fun diff(left: String, right: String) = indexes(left, right)
        .joinToString("") { index -> charDiff(left.getOrNull(index), right.getOrNull(index)) }

private fun indexes(left: String, right: String) = (0 until max(left.length, right.length))

private fun charDiff(lc: Char?, rc: Char?) = if (lc == rc)
    "."
else
    "x"