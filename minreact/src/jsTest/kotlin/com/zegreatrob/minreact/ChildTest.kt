package com.zegreatrob.minreact

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.setup
import react.RProps
import react.children
import react.dom.div
import kotlin.test.Test

class ChildTest {

    data class BoringProps(val content: String) : RProps

    @Test
    fun childSugarWillCorrectlyApplyKeyAndHandler() = setup.invoke(object {
        val innerComponent = reactFunction<BoringProps> { props ->
            props.children()
        }

        val outerComponent = reactFunction<EmptyProps> {
            div {
                child(innerComponent, BoringProps("11"), key = "1") {
                    +"Hello!"
                }
                child(innerComponent, BoringProps("22"), key = "2") {
                    +"Goodbye!"
                }
            }
        }
    }) exercise {
        shallow(outerComponent, EmptyProps())
    } verify { result ->
        val innerComponents = result.find(innerComponent)

        innerComponents.at(0).let {
            it.key().assertIsEqualTo("1")
            it.props().content.assertIsEqualTo("11")
            it.props().children.assertIsEqualTo("Hello!")
        }
        innerComponents.at(1).let {
            it.key().assertIsEqualTo("2")
            it.props().content.assertIsEqualTo("22")
            it.props().children.assertIsEqualTo("Goodbye!")
        }
    }

    @Test
    fun whenPropsAreEmptyChildWillCorrectlyApplyKeyAndHandler() = setup.invoke(object {
        val innerComponent = reactFunction<EmptyProps> { props ->
            props.children()
        }

        val outerComponent = reactFunction<EmptyProps> {
            div {
                child(innerComponent, key = "1") { +"Hello!" }
                child(innerComponent, key = "2") { +"Goodbye!" }
            }
        }
    }) exercise {
        shallow(outerComponent, EmptyProps())
    } verify { result ->
        val innerComponents = result.find(innerComponent)

        innerComponents.at(0).let {
            it.key().assertIsEqualTo("1")
            it.props().children.assertIsEqualTo("Hello!")
        }
        innerComponents.at(1).let {
            it.key().assertIsEqualTo("2")
            it.props().children.assertIsEqualTo("Goodbye!")
        }
    }

}