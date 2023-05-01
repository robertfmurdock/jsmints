package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.Screen
import kotlinx.coroutines.await

interface ByText {
    val screen: Screen
    suspend fun getByText(text: String) = screen.getByText(text)
    suspend fun getAllByText(text: String) = screen.getAllByText(text)
    suspend fun queryByText(text: String) = screen.queryByText(text)
    suspend fun queryAllByText(text: String) = screen.queryAllByText(text)
    suspend fun findByText(text: String) = screen.findByText(text).await()
    suspend fun findAllByText(text: String) = screen.findAllByText(text).await()
}
