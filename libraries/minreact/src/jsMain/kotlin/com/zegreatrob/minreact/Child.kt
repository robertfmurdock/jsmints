package com.zegreatrob.minreact

import org.w3c.dom.Node
import react.ChildrenBuilder
import react.ElementType
import react.Props
import react.PropsWithRef
import react.ReactDsl
import react.create

@Deprecated("Prefer to use standard DSL")
fun <P : PropsWithRef<Node>> ChildrenBuilder.child(
    clazz: ElementType<P>,
    props: P,
    key: String? = null,
    handler: @ReactDsl ChildrenBuilder.() -> Unit = {},
) {
    clazz {
        +props
        this.key = key
        handler()
    }
}

@Deprecated("Prefer to use +.create")
fun <D : DataProps<D>, P> ChildrenBuilder.child(
    dataProps: D,
    key: String? = null,
    handler: @ReactDsl ChildrenBuilder.() -> Unit = {},
) where P : PropsWithRef<Node>,
      P : ChildrenBuilder {
    +dataProps.component.create {
        +dataProps.unsafeCast<Props>()
        key?.let { this.key = it }
        handler()
    }
}

fun <D> ChildrenBuilder.add(
    dataProps: DataProps<in D>,
    key: String? = null,
    handler: @ReactDsl
    (ChildrenBuilder.() -> Unit) = {},
) where D : DataProps<in D> {
    +dataProps.component.create {
        +dataProps.unsafeCast<Props>()
        key?.let { this.key = it }
        handler()
    }
}
