package com.zegreatrob.minreact

import react.ChildrenBuilder
import react.Props
import react.ReactDsl
import react.create

fun <D> ChildrenBuilder.add(
    dataProps: DataProps<in D>,
    key: String? = null,
    handler: @ReactDsl (ChildrenBuilder.() -> Unit) = {},
) where D : DataProps<in D> {
    +dataProps.component.create {
        +dataProps.unsafeCast<Props>()
        key?.let {
            this.key = key
        }
        handler()
    }
}
