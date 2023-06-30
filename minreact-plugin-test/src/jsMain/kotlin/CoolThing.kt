import com.zegreatrob.minreact.Minreact
import react.ChildrenBuilder
import react.FC
import react.Props
import react.dom.html.ReactHTML.div

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

// fun ChildrenBuilder.NiceThing(a: String, b: Int?, c: () -> Unit) {
//     NiceThing {
//         this.a = a
//         this.b = b
//         this.c = c
//     }
// }

@Minreact
val NiceThing = FC<NiceThingProps> {
    +"Nice Thing ${it.a}"
}
