package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.Screen
import kotlinx.coroutines.await

interface ScreenByText : ByText {
    val screen: Screen
    override fun getByText(text: String) = screen.getByText(text)
    override fun getAllByText(text: String) = screen.getAllByText(text)
    override fun queryByText(text: String) = screen.queryByText(text)
    override fun queryAllByText(text: String) = screen.queryAllByText(text)
    override suspend fun findByText(text: String) = screen.findByText(text).await()
    override suspend fun findAllByText(text: String) = screen.findAllByText(text).await()
}
