
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.div

interface SomeInterface {
    fun doThing(): String
}

data class GenericObject<out T>(val thing: T)

external interface GenericProps<Z> : Props where Z : SomeInterface {
    var a: String
    var thingo: GenericObject<Z>
}

@ReactFunc
val GenericThing by nfc<GenericProps<*>> {
    +"Generic Thing ${it.a}"
    div {
        +it.thingo.thing.doThing()
    }
}
