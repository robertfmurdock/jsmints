import com.zegreatrob.minreact.Minreact
import react.ChildrenBuilder
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
