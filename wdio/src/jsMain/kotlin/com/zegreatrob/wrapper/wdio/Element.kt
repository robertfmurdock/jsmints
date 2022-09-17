package com.zegreatrob.wrapper.wdio

import kotlin.js.Json
import kotlin.js.Promise

external class Element {
    fun waitForExist(json: Json): Promise<Unit>
    fun isExisting(): Promise<Boolean>
    fun isEnabled(): Promise<Boolean>
    fun isDisplayed(): Promise<Boolean>
    fun isSelected(): Promise<Boolean>
    fun click(): Promise<Unit>
    fun dragAndDrop(target: Element): Promise<Unit>
    fun getText(): Promise<String>
    fun `$`(@Suppress("UNUSED_PARAMETER") selector: String): Promise<Element>
    fun `$$`(@Suppress("UNUSED_PARAMETER") selector: String): Promise<Array<Element>>
    fun getAttribute(key: String): Promise<String>
    fun clearValue(): Promise<Unit>
    fun keys(keystrokes: String): Promise<Unit>
    fun setValue(value: String): Promise<Unit>
    fun selectByIndex(index: Int): Promise<Unit>
    fun selectByVisibleText(text: String): Promise<Unit>
}
