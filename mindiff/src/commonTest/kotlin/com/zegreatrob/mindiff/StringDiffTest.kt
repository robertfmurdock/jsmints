package com.zegreatrob.mindiff

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class StringDiffTest {

    @Test
    fun givenEqualWillReturnEmptyString() = setup(object {
        val l = "Hello world"
        val r = "Hello world"
    }) exercise {
        stringDiff(l, r)
    } verify { result ->
        result.assertIsEqualTo("")
    }

    class WhenStartsSameEndsDifferent {
        object Setup {
            const val l = "My man"
            const val r = "My lady"
        }

        @Test
        fun willIndicateWhereFirstDifferenceOccurs() = setup(Setup) exercise {
            stringDiff(l, r)
        } verify { result ->
            result.split("\n")[0]
                    .assertIsEqualTo("Difference at index 3.")
        }

        @Test
        fun willShowDifferenceSection() = setup(Setup) exercise {
            stringDiff(l, r)
        } verify { result ->
            result.split("\n")
                    .takeLast(2)
                    .assertIsEqualTo(
                            listOf(
                                    "E: man",
                                    "A: lady"
                            )
                    )
        }
    }

    class WhenMiddleSectionIsDifferent {
        object Setup {
            const val l = "The man dances well."
            const val r = "The lady dances well."
        }

        @Test
        fun willIndicateWhereFirstDifferenceOccurs() = setup(Setup) exercise {
            stringDiff(l, r)
        } verify { result ->
            result.split("\n")[0]
                    .assertIsEqualTo("Difference at index 4.")
        }

        @Test
        fun willShowDifferenceSection() = setup(Setup) exercise {
            stringDiff(l, r)
        } verify { result ->
            result.split("\n")
                    .takeLast(2)
                    .assertIsEqualTo(
                            listOf(
                                    "E: man",
                                    "A: lady"
                            )
                    )
        }
    }

    class WhenThereAreTwoDiscreetDifferences {
        object Setup {
            const val l = "The man dances well and is best at the jig."
            const val r = "The lady dances well and is best at the salsa."
        }

        @Test
        fun willIndicateFirstDifference() = setup(Setup) exercise {
            stringDiff(l, r)
        } verify { result ->
            val lines = result.split("\n")
            lines[0].assertIsEqualTo("Difference at index 4.")
            lines.slice(1..2)
                    .assertIsEqualTo(
                            listOf(
                                    "E: man",
                                    "A: lady"
                            )
                    )
        }

        @Test
        fun willIndicateSecondDifference() = setup(Setup) exercise {
            stringDiff(l, r)
        } verify { result ->
            val lines = result.split("\n")
            lines[3].assertIsEqualTo("Difference at index 39.")
            lines.slice(4..5)
                    .assertIsEqualTo(
                            listOf(
                                    "E: jig",
                                    "A: salsa"
                            )
                    )

        }

    }


}

