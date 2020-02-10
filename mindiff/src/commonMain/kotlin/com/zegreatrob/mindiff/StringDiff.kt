package com.zegreatrob.mindiff

fun stringDiff(l: String, r: String): String {
    val diff = diff(l, r)
    val firstDiffIndex = diff.firstDiffIndex()
    if (firstDiffIndex == -1) {
        return ""
    }

    return (listOf("Difference starts at index $firstDiffIndex.")
            + differentSectionDescription(l, r, firstDiffIndex))
            .joinToString("\n")
}

private fun differentSectionDescription(l: String, r: String, firstDiffIndex: Int): List<String> {
    val reverseDiff = diff(l.reversed(), r.reversed())
    val reverseDiffIndex = reverseDiff.firstDiffIndex()

    return listOf(
            "E: ${l.diffRange(firstDiffIndex, reverseDiffIndex)}",
            "A: ${r.diffRange(firstDiffIndex, reverseDiffIndex)}"
    )
}

private fun String.diffRange(firstDiffIndex: Int, reverseDiffIndex: Int) =
        substring(firstDiffIndex, length - reverseDiffIndex)

private fun String.firstDiffIndex() = indexOf("x")