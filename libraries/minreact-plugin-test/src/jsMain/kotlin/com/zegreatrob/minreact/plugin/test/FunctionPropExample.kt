package com.zegreatrob.minreact.plugin.test

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.useState

external interface FunctionPropExampleProps : Props {
    var children: (f: () -> Unit) -> ReactNode?
}

@ReactFunc
val FunctionPropExample by nfc<FunctionPropExampleProps> { props ->
    val (state, setState) = useState(false)
    div { +"FunctionPropExample" }
    div { +props.children({ setState(true) }) }
    if (state) {
        div { +"Did it" }
    }
}
