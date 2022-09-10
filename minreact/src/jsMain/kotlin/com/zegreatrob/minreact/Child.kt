package com.zegreatrob.minreact

import kotlinx.js.JsoDsl
import org.w3c.dom.Node
import react.ChildrenBuilder
import react.ElementType
import react.Props
import react.PropsWithRef
import react.Ref
import react.create

@Deprecated("Prefer to use standard DSL")
fun <P : PropsWithRef<Node>> ChildrenBuilder.child(
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
fun <D : DataProps<D>, P> ChildrenBuilder.child(
    dataProps: D,
    key: String? = null,
    ref: Ref<Node>? = null,
    handler: ChildrenBuilder.() -> Unit = {}
) where P : PropsWithRef<Node>,
        P : ChildrenBuilder {
    +dataProps.component.create {
        +dataProps.unsafeCast<Props>()
        key?.let { this.key = it }
        ref?.let { this.ref = ref }
        handler()
    }
}

fun <D> ChildrenBuilder.add(
    dataProps: DataProps<in D>,
    key: String? = null,
    ref: Ref<Node>? = null,
    handler: @JsoDsl ChildrenBuilder.() -> Unit = {}
) where D : DataProps<in D> {
    +dataProps.component.create {
        +dataProps.unsafeCast<Props>()
        key?.let { this.key = it }
        ref?.let { this.ref = ref }
        handler()
    }
}
