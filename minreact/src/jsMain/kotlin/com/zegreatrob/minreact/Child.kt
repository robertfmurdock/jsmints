package com.zegreatrob.minreact

import org.w3c.dom.Node
import react.ChildrenBuilder
import react.ElementType
import react.Props
import react.Ref
import react.key
import react.ref

fun <P : Props> ChildrenBuilder.child(
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

fun <P : DataProps<P>> ChildrenBuilder.child(
    dataProps: P,
    key: String? = null,
    ref: Ref<Node>? = null,
    handler: ChildrenBuilder.() -> Unit = {}
) {
    dataProps.component {
        key?.let { this.key = it }
        ref?.let { this.ref = ref }
        +dataProps.unsafeCast<Props>()
        handler()
    }
}
