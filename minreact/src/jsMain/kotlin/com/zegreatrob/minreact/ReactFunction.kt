package com.zegreatrob.minreact

import com.zegreatrob.minreact.external.corejs.objectAssign
import react.*
import kotlin.reflect.KClass

inline fun <reified P : RProps> reactFunction(crossinline function: RBuilder.(P) -> Unit): RClass<P> =
    buildReactFunction(P::class) { props ->
        buildElement { function(props) }
    }

fun <P : RProps> buildReactFunction(kClass: KClass<P>, builder: (props: P) -> ReactElement) = { props: P ->
    ensureKotlinClassProps(props, kClass.js)
        .let(builder)
}.unsafeCast<RClass<P>>()

private fun <P : RProps> ensureKotlinClassProps(props: P, jsClass: JsClass<P>): P = if (props::class.js == jsClass) {
    props
} else {
    val newProps = js("new jsClass()")
    objectAssign(newProps, props)
    newProps.unsafeCast<P>()
}
