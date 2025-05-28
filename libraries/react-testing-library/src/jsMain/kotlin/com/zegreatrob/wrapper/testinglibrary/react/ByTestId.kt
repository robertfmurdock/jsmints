package com.zegreatrob.wrapper.testinglibrary.react

import org.w3c.dom.HTMLElement

interface ByTestId {
    fun getByTestId(text: String): HTMLElement
    suspend fun findByTestId(text: String): HTMLElement
    fun queryByTestId(text: String): HTMLElement?
    fun getAllByTestId(text: String): Array<HTMLElement>
    fun queryAllByTestId(text: String): Array<HTMLElement>
    suspend fun findAllByTestId(text: String): Array<HTMLElement>
}
