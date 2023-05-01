package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.Screen
import kotlinx.coroutines.await

interface ScreenByText : ByText {
    val screen: Screen
    override suspend fun getByText(text: String) = screen.getByText(text)
    override suspend fun getAllByText(text: String) = screen.getAllByText(text)
    override suspend fun queryByText(text: String) = screen.queryByText(text)
    override suspend fun queryAllByText(text: String) = screen.queryAllByText(text)
    override suspend fun findByText(text: String) = screen.findByText(text).await()
    override suspend fun findAllByText(text: String) = screen.findAllByText(text).await()
}
