package com.zegreatrob.minreact

import com.zegreatrob.minreact.external.corejs.objectAssign
import react.*

inline fun <reified P : DataProps> tmFC(crossinline function: ChildrenBuilder.(P) -> Unit):
        ElementType<DataPropsBridge<P>> = FC { props: DataPropsBridge<P> ->
    val newProps = ensureKotlinClassProps(props, P::class.js)
    +(newProps.unsafeCast<Props>())
    function(newProps)
}

fun <P : DataProps> ensureKotlinClassProps(props: DataPropsBridge<P>, jsClass: JsClass<P>): P =
    if (props::class.js == jsClass) {
        props
    } else {
        @Suppress("UNUSED_VARIABLE") val thing = jsClass
        val newProps = js("new thing()")
        objectAssign(newProps, props)
        newProps
    }.unsafeCast<P>()

interface DataProps

external interface DataPropsBridge<P : DataProps> : Props

typealias TMFC <P> = ElementType<DataPropsBridge<P>>

val DataProps.children get() = this.unsafeCast<PropsWithChildren>().children

fun ChildrenBuilder.children(dataProps: DataProps) {
    dataProps.children?.forEach { child(it) }
}