package com.zegreatrob.minreact

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.setup
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import kotlin.test.Test

val funny = tmFC<Funny> { (first, second) ->
    div {
        span { +"$first" }
        span { +second }
    }
}

data class Funny(val first: Int, val second: String) : DataPropsBind<Funny>(funny)

class ReactFunctionTest {

    @Test
    fun canUseDataObjectAndDestructureDuringRender() = setup(object {
        val expectedFirst = 3948
        val expectedSecond = "9922"
    }) exercise {
        shallow(Funny(expectedFirst, expectedSecond))
    } verify { result ->
        result.find<dynamic>("span")
            .map { it.text() }
            .toList()
            .assertIsEqualTo(
                listOf("$expectedFirst", expectedSecond)
            )
    }
}
