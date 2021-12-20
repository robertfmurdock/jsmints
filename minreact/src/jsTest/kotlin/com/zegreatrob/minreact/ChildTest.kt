package com.zegreatrob.minreact

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import react.key
import kotlin.test.Test

external interface BoringProps : PropsWithChildren {
    var content: String
}

class ChildTest {

    @Test
    fun childSugarWillCorrectlyApplyKeyAndHandler() = setup.invoke(object {
        val innerComponent = FC<BoringProps> { props ->
            props.children()
        }

        val outerComponent = tmFC<EmptyProps> {
            div {
                innerComponent {
                    key = "1"
                    content = "11"
                    +"Hello!"
                }
                innerComponent {
                    key = "2"
                    content = "22"
                    +"Goodbye!"
                }
            }
        }
    }) exercise {
        com.zegreatrob.minenzyme.shallow(outerComponent, EmptyProps())
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
        val innerComponent = FC<PropsWithChildren> { props ->
            props.children()
        }

        val outerComponent = tmFC<EmptyProps> {
            div {
                innerComponent { key = "1"; +"Hello!" }
                innerComponent { key = "2"; +"Goodbye!" }
            }
        }
    }) exercise {
        com.zegreatrob.minenzyme.shallow(outerComponent, EmptyProps())
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
