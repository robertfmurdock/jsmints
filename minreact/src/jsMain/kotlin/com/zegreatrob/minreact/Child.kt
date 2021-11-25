package com.zegreatrob.minreact

import org.w3c.dom.Node
import react.*

fun <P : Props> RBuilder.child(
    clazz: ElementType<P>,
    props: P,
    key: String? = null,
    ref: Ref<Node>? = null,
    handler: RHandler<P> = {}
) {
    key?.let { props.key = it }
    ref?.let { props.ref = ref }
    return child(
        type = clazz,
        props = props,
        handler = handler
    )
}

fun RBuilder.child(
    clazz: ElementType<EmptyProps>,
    key: String? = null,
    ref: Ref<Node>? = null,
    handler: RHandler<EmptyProps> = {}
) {
    val props = EmptyProps()
    key?.let { props.key = it }
    ref?.let { props.ref = ref }
    return child(
        type = clazz,
        props = props,
        handler = handler
    )
}
