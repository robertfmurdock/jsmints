package com.zegreatrob.wrapper.testinglibrary.react

import org.w3c.dom.HTMLElement

interface ByText {
    fun getByText(text: String): HTMLElement
    fun getAllByText(text: String): Array<HTMLElement>
    fun queryByText(text: String): HTMLElement?
    fun queryAllByText(text: String): Array<HTMLElement>
    suspend fun findByText(text: String): HTMLElement
    suspend fun findAllByText(text: String): Array<HTMLElement>
}
