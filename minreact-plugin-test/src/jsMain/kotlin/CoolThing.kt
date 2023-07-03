
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.FC
import react.Props
import react.PropsWithChildren
import react.useEffectOnce

external interface NiceThingProps : Props {
    var a: String
    var b: Int?
    var c: () -> Unit
}

@ReactFunc
val NiceThing = FC<NiceThingProps> {
    +"Nice Thing ${it.a}"
}

@ReactFunc
val ZanyThing by nfc<NiceThingProps> {
    +"Zany Thing ${it.a}"
    useEffectOnce { it.c() }
}

external interface WrapperProps : PropsWithChildren {
    var a: String
}

@ReactFunc
val WrapperThing by nfc<WrapperProps> {
    +"Wrapper Thing ${it.a}"
    +it.children
}
