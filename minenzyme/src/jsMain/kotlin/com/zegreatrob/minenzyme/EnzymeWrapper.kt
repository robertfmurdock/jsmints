package com.zegreatrob.minenzyme

import react.*
import kotlin.js.Json
import kotlin.js.json

@JsModule("enzyme")
external val enzyme: Enzyme

@JsModule("enzyme-adapter-react-16")
external class Adapter

val setup = object {
    init {
        enzyme.configure(json("adapter" to Adapter()))
    }
}

external interface Enzyme {
    fun shallow(element: dynamic): ShallowWrapper<dynamic>

    fun configure(config: Json)
}

external interface ShallowWrapper<T> {
    fun <T2 : Props> find(target: ElementType<T2>): ShallowWrapper<T2>
    fun <T2> find(target: dynamic): ShallowWrapper<T2>

    val length: Int

    fun props(): T

    fun update(): ShallowWrapper<T>

    fun debug(): String

    fun text(): String
    fun key(): String?

    fun simulate(eventName: String)
    fun simulate(eventName: String, event: dynamic)

    fun <O> map(mapper: (ShallowWrapper<T>) -> O): Array<O>

    fun <T> find(mapper: (ShallowWrapper<T>) -> Boolean): ShallowWrapper<T>
    fun hasClass(className: String): Boolean
    fun prop(key: String): Any
    fun at(index: Int): ShallowWrapper<T>
    fun shallow(): ShallowWrapper<T>
}

fun <T> ShallowWrapper<T>.simulateInputChange(fieldName: String, fieldValue: String) {
    return findInputByName(fieldName)
        .simulate(
            "change",
            json(
                "target" to json("name" to fieldName, "value" to fieldValue),
                "persist" to {}
            )
        )
}

fun <T> ShallowWrapper<T>.findByClass(className: String) = find<T>(".${className}")
fun <T> ShallowWrapper<T>.findInputByName(inputName: String) = find<T>("input[name='${inputName}']")

fun <P : Props> shallow(reactFunction: ElementType<P>, props: P, handler: RHandler<P> = {}) = enzyme.shallow(buildElement {
    child(reactFunction, props, handler)
})

fun <P : Props, T> shallow(reactFunction: FunctionComponent<P>, props: P) =
    enzyme.shallow(buildElement {
        child(reactFunction, props, {})
    })

fun shallow(handler: RBuilder.() -> Unit) = enzyme.shallow(buildElement(handler))
