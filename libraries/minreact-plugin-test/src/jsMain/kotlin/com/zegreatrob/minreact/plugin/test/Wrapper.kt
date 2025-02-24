package com.zegreatrob.minreact.plugin.test

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import react.useEffect

external interface WrapperProps : PropsWithChildren {
    var a: String?
}

@ReactFunc
val WrapperThing by nfc<WrapperProps> {
    +"Wrapper Thing ${it.a}"
    div {
        +"Children Section"
        +it.children
    }
}

@ReactFunc
val IgnoreChildren by nfc<WrapperProps> {
    +"Ignore ${it.a}"
    div { +"Children Section" }
}

external interface OptionalFuncExampleProps : PropsWithChildren {
    var a: (() -> Unit)?
}

@ReactFunc
val OptionalFuncExample by nfc<OptionalFuncExampleProps> {
    +"OptionalFuncExample"
    useEffect { it.a?.invoke() }
    +it.children
}
