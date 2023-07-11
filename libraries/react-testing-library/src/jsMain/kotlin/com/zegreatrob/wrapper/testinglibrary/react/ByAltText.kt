package com.zegreatrob.wrapper.testinglibrary.react

import org.w3c.dom.HTMLElement

interface ByAltText {
    fun getByAltText(text: String): HTMLElement
    suspend fun findByAltText(text: String): HTMLElement
    fun queryByAltText(text: String): HTMLElement?
    fun getAllByAltText(text: String): Array<HTMLElement>
    fun queryAllByAltText(text: String): Array<HTMLElement>
    suspend fun findAllByAltText(text: String): Array<HTMLElement>
}
