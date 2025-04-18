package com.zegreatrob.minreact.plugin.test

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.dom.html.ReactHTML.div
import react.useEffectOnce

@ReactFunc
val ZanyThing by nfc<NiceThingProps> {
    useEffectOnce { it.c() }
    div { +"Zany Thing ${it.a}" }
}
