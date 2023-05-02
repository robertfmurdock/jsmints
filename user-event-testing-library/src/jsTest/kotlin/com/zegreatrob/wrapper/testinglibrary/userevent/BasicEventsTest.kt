package com.zegreatrob.wrapper.testinglibrary.userevent

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import react.FC
import react.Props
import react.create
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.useState
import kotlin.test.Test

class BasicEventsTest {

    @Test
    fun canHandleClick() = asyncSetup(object {
        val actor = UserEvent.setup()
        val OnButton = FC<Props> {
            var wasPressed by useState(false)
            button {
                +"Button"
                onClick = { wasPressed = true }
            }

            if (wasPressed) {
                +"BOOM!"
            }
        }
        val button get() = screen.getByRole("button", RoleOptions(name = "Button"))
    }) {
        render(OnButton.create())
    } exercise {
        actor.click(button)
    } verify {
        screen.queryByText("BOOM!")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun actorCanTypeIntoInput() = asyncSetup(object {
        val actor = UserEvent.setup()
        val TestComponent = FC<Props> {
            var data by useState("")
            label {
                +"Input"
                input {
                    onChange = { data = it.target.value }
                }
            }

            label {
                +"Data"
                input { value = data }
            }
        }
        val input get() = screen.getByLabelText("Input")
        val expectedText = "abcdefg"
    }) {
        render(TestComponent.create())
    } exercise {
        actor.type(input, expectedText)
    } verify {
        screen.getByLabelText("Data")
            .getAttribute("value")
            .assertIsEqualTo(expectedText)
    }
}
