package com.zegreatrob.minreact.plugin.test

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML.div

external interface ParameterExampleProps<T> : Props {
    var a: T
    var b: Int
    var children: (thing1: T, thing2: Int) -> ReactNode?
}

@ReactFunc
val ParameterExample by nfc<ParameterExampleProps<String>> { props ->
    div { +"ParameterExample" }
    div { +props.children(props.a, props.b) }
}
