import com.example.CoolThing
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import react.FC
import react.Props
import react.create
import kotlin.test.Ignore
import kotlin.test.Test

class AnnotationTest {

    @Test
    @Ignore
    fun canUseComponent() = setup(object {
        val normal = FC<Props> {
            CoolThing(
                a = "Hi",
                b = 7,
                c = { println("DO IT") },
            )
        }
    }) exercise {
        render(normal.create())
    } verify {
        screen.queryByText("Cool Thing Hi")
            .assertIsNotEqualTo(null)
    }
}
