package com.zegreatrob.minreact

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.within
import react.Key
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
        within(screen.getByText("11"))
            .getByText("Hello!")
            .assertIsNotEqualTo(null)
        within(screen.getByText("22"))
            .getByText("Goodbye!")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun createSugarAlsoWorks() = setup(object {
        val outerComponent by nfc<Props> {
            div {
                +BoringComponent("11").create(key = Key("1")) {
                    span { +"Hello!" }
                }
                +BoringComponent("22").create(key = Key("2")) {
                    span { +"Goodbye!" }
                }
            }
        }
    }) exercise {
        render(outerComponent.create {})
    } verify {
        within(screen.getByText("11"))
            .getByText("Hello!")
            .assertIsNotEqualTo(null)
        within(screen.getByText("22"))
            .getByText("Goodbye!")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun nfcAlwaysUsesSameReactFunction() = setup(object {
        val outerComponent by nfc<Props> {
            div {
                +BoringComponent("11").create(key = Key("1")) {
                    span { +"Hello!" }
                }
                +BoringComponent("22").create(key = Key("2")) {
                    span { +"Goodbye!" }
                }
            }
        }
    }) exercise {
        outerComponent
    } verify { result ->
        result.assertIsEqualTo(outerComponent)
    }
}
