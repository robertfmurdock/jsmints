import com.zegreatrob.minreact.ReactFunc
import react.FC
import react.Props

external interface NiceThingProps : Props {
    var a: String
    var b: Int?
    var c: () -> Unit
}

@ReactFunc
val NiceThing = FC<NiceThingProps> {
    +"Nice Thing ${it.a}"
}
