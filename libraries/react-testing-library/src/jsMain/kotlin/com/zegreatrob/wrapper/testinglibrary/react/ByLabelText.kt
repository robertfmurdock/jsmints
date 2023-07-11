package com.zegreatrob.wrapper.testinglibrary.react

import org.w3c.dom.HTMLElement

interface ByLabelText {
    fun getByLabelText(text: String): HTMLElement
    fun queryByLabelText(text: String): HTMLElement?
    fun getAllByLabelText(text: String): Array<HTMLElement>
    fun queryAllByLabelText(text: String): Array<HTMLElement>
    suspend fun findByLabelText(text: String): HTMLElement
    suspend fun findAllByLabelText(text: String): Array<HTMLElement>
}
