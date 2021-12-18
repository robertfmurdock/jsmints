package com.zegreatrob.minreact

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.setup
import react.PropsWithChildren
import react.dom.div
import react.fc
import kotlin.test.Test

external interface BoringProps : PropsWithChildren {
    var content: String
}

class ChildTest {

    @Test
    fun childSugarWillCorrectlyApplyKeyAndHandler() = setup.invoke(object {
        val innerComponent = fc<BoringProps> { props ->
            props.children()
        }

        val outerComponent = reactFunction<EmptyProps> {
            div {
                child(innerComponent) {
                    key = "1"
                    attrs.content = "11"
                    +"Hello!"
                }
                child(innerComponent) {
                    key = "2"
                    attrs.content = "22"
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
        val innerComponent = fc<PropsWithChildren> { props ->
            props.children()
        }

        val outerComponent = reactFunction<EmptyProps> {
            div {
                child(innerComponent) { key = "1"; +"Hello!" }
                child(innerComponent) { key = "2"; +"Goodbye!" }
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