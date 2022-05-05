package com.zegreatrob.minreact

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.dataprops
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.setup
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import kotlin.test.Test

val boringComponent = tmFC<BoringComponent> { props ->
    children(props)
}

data class BoringComponent(var content: String) : DataPropsBind<BoringComponent>(boringComponent)

class ChildTest {

    @Test
    fun childSugarWillCorrectlyApplyKeyAndHandler() = setup(object {
        val outerComponent = FC<Props> {
            div {
                child(BoringComponent("11"), key = "1") {
                    +"Hello!"
                }
                child(BoringComponent("22"), key = "2") {
                    +"Goodbye!"
                }
            }
        }
    }) exercise {
        shallow(outerComponent)
    } verify { result ->
        val innerComponents = result.find(boringComponent)

        innerComponents.at(0).let {
            it.key().assertIsEqualTo("1")
            it.dataprops().content.assertIsEqualTo("11")
            it.dataprops().children.assertIsEqualTo("Hello!")
        }
        innerComponents.at(1).let {
            it.key().assertIsEqualTo("2")
            it.dataprops().content.assertIsEqualTo("22")
            it.dataprops().children.assertIsEqualTo("Goodbye!")
        }
    }
}
