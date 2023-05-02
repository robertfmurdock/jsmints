package com.zegreatrob.minreact

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import react.dom.events.MouseEvent
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.useState
import web.html.HTMLButtonElement
import kotlin.test.Test

class BasicEventsTest {

    @Test
    fun canHandleClick() = asyncSetup(object {
        val actor = UserEvent.setup()
        val button get() = screen.getByRole("button", RoleOptions(name = "Button"))
    }) {
        render(OnButton(false).create())
    } exercise {
        actor.click(button)
    } verify {
        screen.queryByText("BOOM!")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun actorCanTypeIntoInput() = asyncSetup(object {
        val actor = UserEvent.setup()

        val input get() = screen.getByLabelText("Input")
        val expectedText = "abcdefg"
    }) {
        render(TestComponent("").create())
    } exercise {
        actor.type(input, expectedText)
    } verify {
        screen.getByLabelText("Data")
            .getAttribute("value")
            .assertIsEqualTo(expectedText)
    }
}

val onButton by ntmFC<OnButton> { props ->
    var wasPressed by useState(props.startState)
    val function: (MouseEvent<HTMLButtonElement, *>) -> Unit = { wasPressed = true }
    button {
        +"Button"
        onClick = function
    }
    if (wasPressed) {
        +"BOOM!"
    }
}

val testComponent by ntmFC<TestComponent> { props ->
    var data by useState(props.startState)
    label {
        +"Input"
        input {
            onChange = { data = it.target.value }
        }
    }

    label {
        +"Data"
        input {
            readOnly = true
            value = data
        }
    }
}

data class TestComponent(val startState: String) : DataPropsBind<TestComponent>(testComponent)

data class OnButton(val startState: Boolean) : DataPropsBind<OnButton>(onButton)
