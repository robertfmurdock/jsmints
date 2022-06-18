package com.zegreatrob.minreact

import com.zegreatrob.minreact.external.corejs.objectAssign
import kotlinx.js.JsoDsl
import react.Children
import react.ChildrenBuilder
import react.ElementType
import react.FC
import react.Props
import react.PropsWithChildren
import react.create

inline fun <reified P : DataProps<P>> tmFC(crossinline function: ChildrenBuilder.(P) -> Unit):
    ElementType<DataPropsBridge<P>> = FC { props: DataPropsBridge<P> ->
    val newProps = ensureKotlinClassProps(props, P::class.js)
    +(newProps.unsafeCast<Props>())
    function(newProps)
}

fun <P : DataProps<P>> ensureKotlinClassProps(props: DataPropsBridge<P>, jsClass: JsClass<P>): P =
    if (props::class.js == jsClass) {
        props
    } else {
        @Suppress("UNUSED_VARIABLE") val thing = jsClass
        val newProps = js("new thing()")
        objectAssign(newProps, props)
        newProps
    }.unsafeCast<P>()

interface DataProps<P : DataProps<P>> {
    val component: TMFC<P>
}

external interface DataPropsBridge<P : DataProps<P>> : Props

typealias TMFC <P> = ElementType<DataPropsBridge<P>>

val DataProps<*>.children get() = this.unsafeCast<PropsWithChildren>().children

fun ChildrenBuilder.children(DataProps: DataProps<*>) {
    Children.toArray(DataProps.children).forEach(::child)
}

fun <P : DataProps<P>, PP> create(dataProps: DataProps<P>, block: @JsoDsl PP.() -> Unit = {})
        where PP : Props, PP : ChildrenBuilder = dataProps.component.create {
    +dataProps.unsafeCast<Props>()
    block(unsafeCast<PP>())
}

fun <P> DataProps<in P>.create(block: @JsoDsl P.() -> Unit = {})
        where P : DataProps<in P>, P : Props, P : ChildrenBuilder = create(this, block)
