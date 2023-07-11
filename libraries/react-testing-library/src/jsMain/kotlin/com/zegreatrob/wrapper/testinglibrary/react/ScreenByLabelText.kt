package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.Screen
import kotlinx.coroutines.await

interface ScreenByLabelText : ByLabelText {

    val screen: Screen

    override fun getByLabelText(text: String) = screen.getByLabelText(text)
    override fun queryByLabelText(text: String) = screen.queryByLabelText(text)
    override fun getAllByLabelText(text: String) = screen.getAllByLabelText(text)
    override fun queryAllByLabelText(text: String) = screen.queryAllByLabelText(text)
    override suspend fun findByLabelText(text: String) = screen.findByLabelText(text).await()
    override suspend fun findAllByLabelText(text: String) = screen.findAllByLabelText(text).await()
}
