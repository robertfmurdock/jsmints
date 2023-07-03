import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.PropsWithChildren

external interface WrapperProps : PropsWithChildren {
    var a: String
}

@ReactFunc
val WrapperThing by nfc<WrapperProps> {
    +"Wrapper Thing ${it.a}"
    +it.children
}
