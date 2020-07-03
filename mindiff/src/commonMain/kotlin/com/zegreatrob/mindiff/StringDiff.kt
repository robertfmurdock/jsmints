package com.zegreatrob.mindiff

import kotlin.math.min

fun stringDiff(l: String, r: String): String {
    val diff = diff(l, r)
    val firstDiffIndex = diff.firstDiffIndex()
    if (firstDiffIndex == -1) {
        return ""
    }

    return differentSectionDescription(l, r, firstDiffIndex)
        .joinToString("\n")
}

private fun differentSectionDescription(l: String, r: String, firstDiffIndex: Int): List<String> {
    val reverseDiff = diff(l.reversed(), r.reversed())
    val reverseDiffIndex = reverseDiff.firstDiffIndex()

    val eDiffRange = l.diffRange(firstDiffIndex, reverseDiffIndex)
    val aDiffRange = r.diffRange(firstDiffIndex, reverseDiffIndex)

    return if (eDiffRange.length > 20) {
        splitIntoTwoDiffSections(firstDiffIndex, eDiffRange, aDiffRange)
    } else {
        diffDescription(index = firstDiffIndex, eDiff = eDiffRange, aDiff = aDiffRange)
    }
}

private fun splitIntoTwoDiffSections(originalFirstDiff: Int, eDiffRange: String, aDiffRange: String): List<String> {
    for (eIndex in 0 until min(eDiffRange.length, 20)) {
        for (aIndex in 0 until min(aDiffRange.length, 20)) {

            val diff = diff(eDiffRange.substring(eIndex), aDiffRange.substring(aIndex))
            val innerFirstDiffIndex = diff.firstDiffIndex()

            if (innerFirstDiffIndex > 2) {
                return diffDescription(
                    index = originalFirstDiff,
                    eDiff = eDiffRange.substring(0 until eIndex),
                    aDiff = aDiffRange.substring(0 until aIndex)
                ) + diffDescription(
                    index = originalFirstDiff + eIndex + innerFirstDiffIndex,
                    eDiff = eDiffRange.substring(eIndex + innerFirstDiffIndex),
                    aDiff = aDiffRange.substring(aIndex + innerFirstDiffIndex)
                )
            }
        }
    }
    return emptyList()
}

private fun diffDescription(index: Int, eDiff: String, aDiff: String) = listOf(
    "Difference at index $index.",
    "E: $eDiff",
    "A: $aDiff"
)

private fun String.diffRange(firstDiffIndex: Int, reverseDiffIndex: Int) = substring(
    firstDiffIndex until length - reverseDiffIndex
)

private fun String.firstDiffIndex() = indexOf("x")