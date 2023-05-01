package com.zegreatrob.wrapper.testinglibrary.react

import org.w3c.dom.HTMLElement

interface ByLabelText {
    suspend fun getByLabelText(text: String): HTMLElement

    suspend fun findByLabelText(text: String): HTMLElement

    suspend fun queryByLabelText(text: String): HTMLElement?

    suspend fun getAllByLabelText(text: String): Array<HTMLElement>

    suspend fun queryAllByLabelText(text: String): Array<HTMLElement>

    suspend fun findAllByLabelText(text: String): Array<HTMLElement>
}
