package com.zegreatrob.minreact.plugin.test

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.useEffectOnce

@ReactFunc
val DestructureThing by nfc<NiceThingProps> { (a, _, c) ->
    +"Destructure Thing $a"
    useEffectOnce { c() }
}
