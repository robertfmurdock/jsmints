package com.zegreatrob.minreact

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.minreact.external.testinglibrary.react.render
import com.zegreatrob.minreact.external.testinglibrary.react.screen
import com.zegreatrob.testmints.setup
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import kotlin.test.Test

val funny by ntmFC<Funny> { (first, second) ->
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
        render(Funny(expectedFirst, expectedSecond).create())
    } verify {
        screen.getByText("$expectedFirst")
            .assertIsNotEqualTo(null)
        screen.getByText(expectedSecond)
            .assertIsNotEqualTo(null)
    }

    @Test
    fun alwaysUsesTheSameReactFunction() = setup(object {
    }) exercise {
        Funny(1, "2").component
    } verify { result ->
        result.assertIsEqualTo(Funny(3, "4").component)
    }
}
