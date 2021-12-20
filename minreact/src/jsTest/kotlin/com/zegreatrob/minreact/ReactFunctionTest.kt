package com.zegreatrob.minreact

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.setup
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import kotlin.test.Test

class ReactFunctionTest {

    data class FunProps(val first: Int, val second: String) : DataProps

    @Test
    fun canUseDataObjectAndDestructureDuringRender() = setup(object {
        val component = tmFC<FunProps> { (first, second) ->
            div {
                span { +"$first" }
                span { +second }
            }
        }
        val expectedFirst = 3948
        val expectedSecond = "9922"
    }) exercise {
        shallow(component, FunProps(expectedFirst, expectedSecond))
    } verify { result ->
        result.find<dynamic>("span")
            .map { it.text() }
            .toList()
            .assertIsEqualTo(
                listOf("$expectedFirst", expectedSecond)
            )
    }

}
