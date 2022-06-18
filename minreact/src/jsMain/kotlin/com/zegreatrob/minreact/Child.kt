package com.zegreatrob.minreact

import kotlinx.js.JsoDsl
import org.w3c.dom.Node
import react.ChildrenBuilder
import react.ElementType
import react.Props
import react.Ref
import react.key
import react.ref

@Deprecated("Prefer to use standard DSL")
fun <P : Props> ChildrenBuilder.child(
    clazz: ElementType<P>,
    props: P,
    key: String? = null,
    ref: Ref<Node>? = null,
    handler: ChildrenBuilder.() -> Unit = {}
) {
    clazz {
        +props
        this.key = key
        this.ref = ref
        handler()
    }
}

@Deprecated("Prefer to use +.create")
fun <P : DataProps<P>> ChildrenBuilder.child(
    dataProps: P,
    key: String? = null,
    ref: Ref<Node>? = null,
    handler: ChildrenBuilder.() -> Unit = {}
) {
    +dataProps.create {
        key?.let { this.key = it }
        ref?.let { this.ref = ref }
        handler()
    }
}

fun <P> ChildrenBuilder.add(dataProps: DataProps<in P>, handler: @JsoDsl P.() -> Unit = {})
        where P : DataProps<in P>, P : Props, P : ChildrenBuilder {
    +dataProps.create {
        handler()
    }
}
