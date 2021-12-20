package com.zegreatrob.minreact

import com.zegreatrob.minreact.external.corejs.objectAssign
import react.ChildrenBuilder
import react.ElementType
import react.FC
import react.Props

inline fun <reified P : Props> tmFC(crossinline function: ChildrenBuilder.(P) -> Unit): ElementType<P> =
    FC { props: P ->
        val newProps = ensureKotlinClassProps(props, P::class.js)
        +newProps
        function(newProps)
    }

fun <P : Props> ensureKotlinClassProps(props: P, jsClass: JsClass<P>): P = if (props::class.js == jsClass) {
    props
} else {
    @Suppress("UNUSED_VARIABLE") val thing = jsClass
    val newProps = js("new thing()")
    objectAssign(newProps, props)
    newProps.unsafeCast<P>()
}
