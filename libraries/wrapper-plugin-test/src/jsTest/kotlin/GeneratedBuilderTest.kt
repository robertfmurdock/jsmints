import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import js.core.JsoDsl
import js.core.jso
import kotlin.test.Test

class GeneratedBuilderTest {

    @Test
    fun canUseGeneratedBuilder() = setup(object {
        val inputString = "abcdefg"
        val inputNumber = 7
    }) exercise {
        ExampleJavascriptObject(inputString, inputNumber)
    } verify { result ->
        result.assertMatchesJso {
            firstThing = inputString
            optionalThing = inputNumber
        }
    }

    private fun <T> T.assertMatchesJso(block: @JsoDsl T.() -> Unit) {
        JSON.stringify(this).assertIsEqualTo(JSON.stringify(jso(block)))
    }

    @Test
    fun canDestructure() = setup(object {
        val inputString = "abcdefg"
        val inputNumber = 7
    }) exercise {
        ExampleJavascriptObject(inputString, inputNumber)
    } verify { (firstThing, optionalThing) ->
        firstThing.assertIsEqualTo(inputString)
        optionalThing.assertIsEqualTo(inputNumber)
    }
}
