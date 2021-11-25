package com.zegreatrob.minreact

import com.zegreatrob.minreact.external.corejs.objectAssign
import react.*
import kotlin.reflect.KClass

inline fun <reified P : Props> reactFunction(crossinline function: RBuilder.(P) -> Unit): ElementType<P> =
    buildReactFunction(P::class) { props ->
        buildElement { function(props) }
    }

fun <P : Props> buildReactFunction(kClass: KClass<P>, builder: (props: P) -> ReactElement) = { props: P ->
    ensureKotlinClassProps(props, kClass.js)
        .let(builder)
}.unsafeCast<ElementType<P>>()

private fun <P : Props> ensureKotlinClassProps(props: P, jsClass: JsClass<P>): P = if (props::class.js == jsClass) {
    props
} else {
    @Suppress("UNUSED_VARIABLE") val thing = jsClass
    val newProps = js("new thing()")
    objectAssign(newProps, props)
    newProps.unsafeCast<P>()
}
