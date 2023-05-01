package com.zegreatrob.wrapper.testinglibrary.react

import org.w3c.dom.HTMLElement

interface ByText {
    suspend fun getByText(text: String): HTMLElement

    suspend fun getAllByText(text: String): Array<HTMLElement>

    suspend fun queryByText(text: String): HTMLElement?

    suspend fun queryAllByText(text: String): Array<HTMLElement>

    suspend fun findByText(text: String): HTMLElement

    suspend fun findAllByText(text: String): Array<HTMLElement>
}
