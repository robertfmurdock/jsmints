package com.zegreatrob.minreact

import org.w3c.dom.Node
import react.*

fun <P : PropsWithChildren> ChildrenBuilder.child(
    clazz: ElementType<P>,
    props: P,
    key: String? = null,
    ref: Ref<Node>? = null,
    handler: ChildrenBuilder.() -> Unit = {}
) {
    key?.let { props.key = it }
    ref?.let { props.ref = ref }
    clazz {
        +props
        handler()
    }
}

fun ChildrenBuilder.child(
    clazz: ElementType<EmptyProps>,
    key: String? = null,
    ref: Ref<Node>? = null,
    handler: ChildrenBuilder.() -> Unit = {}
) {
    val props = EmptyProps()
    key?.let { props.key = it }
    ref?.let { props.ref = ref }
    return clazz {
        +props
        handler()
    }
}
