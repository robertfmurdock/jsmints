package com.zegreatrob.minreact

import com.zegreatrob.minreact.external.corejs.objectAssign
import js.objects.JsoDsl
import react.ChildrenBuilder
import react.ElementType
import react.FC
import react.Key
import react.Props
import react.PropsWithChildren
import react.ReactDsl
import react.ReactNode
import react.create
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

inline fun <reified T : DataProps<T>> ntmFC(noinline function: @ReactDsl ChildrenBuilder.(T) -> Unit) = NamedTmFC(T::class, function)

class NamedTmFC<T : DataProps<T>>(private val clazz: KClass<T>, private val function: @ReactDsl ChildrenBuilder.(T) -> Unit) {

    private var fc: FC<DataPropsBridge>? = null

    operator fun getValue(t: Any?, property: KProperty<*>): FC<DataPropsBridge> = fc ?: FC(property.name) { props: DataPropsBridge ->
        val newProps = ensureKotlinClassProps(props, clazz.js)
        objectAssign(this@FC, newProps)
        function(newProps)
    }.also {
        fc = it
    }
}

fun <T : Props> nfc(function: @ReactDsl ChildrenBuilder.(T) -> Unit) = NamedFC(function)

class NamedFC<T : Props>(private val function: @ReactDsl ChildrenBuilder.(T) -> Unit) {
    private var fc: FC<T>? = null
    operator fun <A> getValue(t: A?, property: KProperty<*>): FC<T> = fc ?: FC(property.name, function).also { fc = it }
}

fun <P : DataProps<P>> ensureKotlinClassProps(props: DataPropsBridge, jsClass: JsClass<P>): P = if (props::class.js == jsClass) {
    props
} else {
    @Suppress("UNUSED_VARIABLE")
    val thing = jsClass
    val newProps = js("new thing()")
    objectAssign(newProps, props)
    newProps
}.unsafeCast<P>()

interface DataProps<P : DataProps<P>> {
    val component: TMFC
}

external interface DataPropsBridge : PropsWithChildren

typealias TMFC = ElementType<DataPropsBridge>

val DataProps<*>.children get() = this.unsafeCast<PropsWithChildren>().children

fun ChildrenBuilder.children(dataProps: DataProps<*>) = +dataProps.children

fun <P : DataProps<P>> create(dataProps: DataProps<P>, block: @JsoDsl Props.() -> Unit = {}) = dataProps.component.create {
    +dataProps.unsafeCast<Props>()
    block(unsafeCast<Props>())
}

fun <D : DataProps<in D>> DataProps<in D>.create(
    key: Key? = null,
    block: @ReactDsl
    (ChildrenBuilder.() -> Unit) = {},
): ReactNode {
    val dataProps = this
    return component.create {
        +dataProps.unsafeCast<Props>()
        key?.let { this.key = it }

        block()
    }
}
