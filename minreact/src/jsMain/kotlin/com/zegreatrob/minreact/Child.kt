package com.zegreatrob.minreact

import org.w3c.dom.Node
import react.*

fun <P : RProps> RBuilder.child(
    clazz: RClass<P>,
    props: P,
    key: String? = null,
    ref: RMutableRef<Node?>? = null,
    handler: RHandler<P> = {}
): ReactElement {
    key?.let { props.key = it }
    ref?.let { props.ref = ref }
    return child(
        type = clazz,
        props = props,
        handler = handler
    )
}

fun RBuilder.child(
    clazz: RClass<EmptyProps>,
    key: String? = null,
    ref: RMutableRef<Node?>? = null,
    handler: RHandler<EmptyProps> = {}
): ReactElement {
    val props = EmptyProps()
    key?.let { props.key = it }
    ref?.let { props.ref = ref }
    return child(
        type = clazz,
        props = props,
        handler = handler
    )
}
