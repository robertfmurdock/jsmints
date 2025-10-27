package com.zegreatrob.minreact.plugin.test

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.within
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.useState
import kotlin.test.Test

class AnnotationTest {

    @Test
    fun canUseComponent2() = setup(object {
        val normal = FC<Props> {
            NiceThing(
                a = "Hi",
                b = 7,
                c = { println("DO IT") },
            )
        }
    }) exercise {
        render(normal.create())
    } verify {
        screen.queryByText("Nice Thing Hi")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun canUseNamedComponent() = setup(object {
        val normal = FC<Props> {
            ZanyThing(
                a = "Hi",
                b = 7,
                c = { println("DO IT") },
            )
        }
    }) exercise {
        render(normal.create())
    } verify {
        screen.queryByText("Zany Thing Hi")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun providesDestructuringFunctions() = setup(object {
        val normal = FC<Props> {
            DestructureThing(
                a = "Hi",
                b = 7,
                c = { println("DO IT") },
            )
        }
    }) exercise {
        render(normal.create())
    } verify {
        screen.queryByText("Destructure Thing Hi")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun canUseKeyToForceRerender() = asyncSetup(object {
        var callCount = 0
        val counter: () -> Unit = { callCount++ }
        val normal = FC<Props> {
            var state by useState(0)

            button {
                +"Button"
                onClick = { state++ }
            }
            ZanyThing(
                a = "Hi",
                b = 7,
                c = counter,
                key = "$state",
            )
        }
        val actor = UserEvent.setup()
    }) {
        render(normal.create())
    } exercise {
        actor.click(screen.findByText("Button"))
    } verify {
        screen.queryByText("Zany Thing Hi")
            .assertIsNotEqualTo(null)
        callCount.assertIsEqualTo(2)
    }

    @Test
    fun canSkipKeys() = asyncSetup(object {
        val normal = FC<Props> {
            ZanyThing(
                a = "Hi1",
                b = 7,
                c = { },
            )
            ZanyThing(
                a = "Hi2",
                b = 7,
                c = { },
            )
            ZanyThing(
                a = "Hi3",
                b = 7,
                c = { },
            )
        }
    }) exercise {
        render(normal.create())
    } verify {
        screen.queryByText("Zany Thing Hi1")
            .assertIsNotEqualTo(null)
        screen.queryByText("Zany Thing Hi2")
            .assertIsNotEqualTo(null)
        screen.queryByText("Zany Thing Hi3")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun canUseComponentWithChildren() = setup(object {
        val normal = FC<Props> {
            WrapperThing(a = "Hi") {
                div { +"We are children" }
            }
        }
    }) exercise {
        render(normal.create())
    } verify {
        screen.queryByText("Wrapper Thing Hi")
            .assertIsNotEqualTo(null)
        within(screen.queryByText("Children Section"))
            .queryByText("We are children")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun canUseComponentThatDisabledChildren() = setup(object {
        val normal = FC<Props> {
            IgnoreChildren(a = "Hi") {
                div { +"We are children" }
            }
        }
    }) exercise {
        render(normal.create())
    } verify {
        screen.queryByText("Ignore Hi")
            .assertIsNotEqualTo(null, "component not found")
        within(screen.queryByText("Children Section"))
            .queryByText("We are children")
            .assertIsEqualTo(null, "children were included even though component does not use them")
    }

    @Test
    fun canUseComponentWithOptionalFunction() = setup(object {
        val normal = FC<Props> {
            OptionalFuncExample()
        }
    }) exercise {
        render(normal.create())
    } verify {
        screen.queryByText("OptionalFuncExample")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun canUseComponentWithChildrenSkippingTheChildren() = setup(object {
        val normal = FC<Props> {
            WrapperThing(a = "Hi")
        }
    }) exercise {
        render(normal.create())
    } verify {
        screen.queryByText("Wrapper Thing Hi")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun canUseComponentWithGenericProps() = setup(object {
        val normal = FC<Props> {
            GenericThing(
                a = "Hi",
                thingo = GenericObject(
                    object : SomeInterface {
                        override fun doThing() = "Thing do"
                    },
                ),
            )
        }
    }) exercise {
        render(normal.create())
    } verify {
        screen.queryByText("Generic Thing Hi")
            .assertIsNotEqualTo(null)
        screen.queryByText("Thing do")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun createFunctionWillReturnReactNode() = setup(object {
        val node: ReactNode = GenericThing.create(
            a = "Hi",
            thingo = GenericObject(
                object : SomeInterface {
                    override fun doThing() = "Thing do"
                },
            ),
        )
    }) exercise {
        render(node)
    } verify {
        screen.queryByText("Generic Thing Hi")
            .assertIsNotEqualTo(null)
        screen.queryByText("Thing do")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun canUseChildrenParameterAsChildrenBuilder() = asyncSetup(object {
        val expectedFirstValue = "Here is a value"
        val expectedSecondValue = 7
        val normal = FC<Props> {
            ParameterExample(expectedFirstValue, expectedSecondValue) { value1, value2 ->
                span { +value1 }
                div { +"$value2" }
            }
        }
    }) exercise {
        render(normal.create())
    } verify {
        screen.queryByText("ParameterExample")
            .assertIsNotEqualTo(null, "did not find component")
        screen.queryByText(expectedFirstValue)
            .assertIsNotEqualTo(null, "did not find first value")
        screen.queryByText("$expectedSecondValue")
            .assertIsNotEqualTo(null, "did not find second value")
    }

    @Test
    fun canUseNoArgFunctionAsParameterOfFunctionInProp() = asyncSetup(object {
        var lastTrigger: (() -> Unit)? = null
        val normal = FC<Props> {
            FunctionPropExample { trigger ->
                lastTrigger = trigger
            }
        }
    }) {
        render(normal.create())
    } exercise {
        act { lastTrigger?.invoke() }
    } verify {
        screen.queryByText("Did it")
            .assertIsNotEqualTo(null, "did not find component")
    }
}
