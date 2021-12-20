package com.zegreatrob.minreact

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.dataprops
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.setup
import react.dom.html.ReactHTML.div
import kotlin.test.Test

data class BoringProps(var content: String) : DataProps

class ChildTest {

    @Test
    fun childSugarWillCorrectlyApplyKeyAndHandler() = setup.invoke(object {
        val innerComponent = tmFC<BoringProps> { props ->
            children(props)
        }
        val outerComponent = tmFC<EmptyProps> {
            div {
                child(innerComponent, BoringProps("11"), key = "1" ) {
                    +"Hello!"
                }
                child(innerComponent, BoringProps("22"), key = "2") {
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
            it.dataprops().content.assertIsEqualTo("11")
            it.dataprops().children.assertIsEqualTo("Hello!")
        }
        innerComponents.at(1).let {
            it.key().assertIsEqualTo("2")
            it.dataprops().content.assertIsEqualTo("22")
            it.dataprops().children.assertIsEqualTo("Goodbye!")
        }
    }

}
