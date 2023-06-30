
import com.zegreatrob.minreact.Minreact
import com.zegreatrob.minreact.nfc
import react.ChildrenBuilder
import react.FC
import react.Props
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import react.useEffectOnce

@Minreact
@Suppress("unused")
object CoolThing : MRFunc<CoolThing.Props>(
    component = {
        div {
            +"Cool Thing ${it.a}"
        }
    },
) {
    data class Props(
        val a: String,
        val b: Int?,
        val c: () -> Unit,
    )
}

abstract class MRFunc<P>(val component: ChildrenBuilder.(P) -> Unit)

external interface NiceThingProps : Props {
    var a: String
    var b: Int?
    var c: () -> Unit
}

@Minreact
val NiceThing = FC<NiceThingProps> {
    +"Nice Thing ${it.a}"
}

@Minreact
val ZanyThing by nfc<NiceThingProps> {
    +"Zany Thing ${it.a}"
    useEffectOnce { it.c() }
}

external interface WrapperProps : PropsWithChildren {
    var a: String
}

@Minreact
val WrapperThing by nfc<WrapperProps> {
    +"Wrapper Thing ${it.a}"
    +it.children
}
