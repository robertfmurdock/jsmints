
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import com.zegreatrob.minreact.plugin.test.NiceThingProps
import react.useEffectOnce

@ReactFunc
val ZanyThing by nfc<NiceThingProps> {
    +"Zany Thing ${it.a}"
    useEffectOnce { it.c() }
}
