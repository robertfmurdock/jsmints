package com.zegreatrob.wrapper.wdio

import kotlin.js.Json
import kotlin.js.Promise

external interface Browser {
    val options: Json
    fun `$`(selector: String): Promise<Element>
    fun `$$`(selector: String): Promise<Array<Element>>
    fun waitUntil(condition: () -> Promise<Boolean>, options: Json): Promise<Unit>
    fun getUrl(): Promise<String>
    fun acceptAlert(): Promise<Unit>
    fun dismissAlert(): Promise<Unit>
    fun getAlertText(): Promise<String>

    fun navigateTo(location: String): Promise<Unit>
    fun url(url: String): Promise<Unit>

    fun getLogs(s: String): Promise<Array<Json>>
    fun isAlertOpen(): Promise<Boolean>
    fun execute(code: (dynamic) -> dynamic, argument: dynamic)
    fun executeAsync(arg: (dynamic, () -> Unit) -> dynamic, argument: dynamic): Promise<Unit>
    fun refresh(): Promise<Unit>

    fun on(event: String, listener: (dynamic) -> Promise<Any?>)
    fun once(event: String, listener: (dynamic) -> Promise<Any?>)
    fun emit(event: String)
    fun removeListener(event: String, listener: Any)
    fun removeAllListeners(event: String)
}
