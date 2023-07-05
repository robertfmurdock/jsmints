
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.minreact.plugin.test.DestructureThing
import com.zegreatrob.minreact.plugin.test.NiceThing
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import react.FC
import react.Props
import react.create
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
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
        screen.queryByText("We are children")
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
}
