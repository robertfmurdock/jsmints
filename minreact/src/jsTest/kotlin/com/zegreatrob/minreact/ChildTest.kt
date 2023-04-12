package com.zegreatrob.minreact

import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.minreact.external.testinglibrary.react.getByText
import com.zegreatrob.minreact.external.testinglibrary.react.render
import com.zegreatrob.minreact.external.testinglibrary.react.screen
import com.zegreatrob.testmints.setup
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.span
import kotlin.test.Test

val boringComponent by ntmFC<BoringComponent> { props ->
    label {
        +props.content
        children(props)
    }
}

data class BoringComponent(var content: String) : DataPropsBind<BoringComponent>(boringComponent)

class ChildTest {

    @Test
    fun addSugarWillCorrectlyApplyKeyAndHandler() = setup(object {
        val outerComponent by nfc<Props> {
            div {
                add(BoringComponent("11"), key = "1") {
                    span { +"Hello!" }
                }
                add(BoringComponent("22"), key = "2") {
                    span { +"Goodbye!" }
                }
            }
        }
    }) exercise {
        render(outerComponent.create {})
    } verify {
        getByText(screen.getByText("11"), "Hello!")
            .assertIsNotEqualTo(null)
        getByText(screen.getByText("22"), "Goodbye!")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun createSugarAlsoWorks() = setup(object {
        val outerComponent by nfc<Props> {
            div {
                +BoringComponent("11").create(key = "1") {
                    span { +"Hello!" }
                }
                +BoringComponent("22").create(key = "2") {
                    span { +"Goodbye!" }
                }
            }
        }
    }) exercise {
        render(outerComponent.create {})
    } verify {
        getByText(screen.getByText("11"), "Hello!")
            .assertIsNotEqualTo(null)
        getByText(screen.getByText("22"), "Goodbye!")
            .assertIsNotEqualTo(null)
    }
}
