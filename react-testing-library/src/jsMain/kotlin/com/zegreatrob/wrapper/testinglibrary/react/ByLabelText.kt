package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.Screen
import kotlinx.coroutines.await

interface ByLabelText {

    val screen: Screen

    suspend fun getByLabelText(text: String) = screen.getByLabelText(text)
    suspend fun findByLabelText(text: String) = screen.findByLabelText(text).await()
    suspend fun queryByLabelText(text: String) = screen.queryByLabelText(text)
    suspend fun getAllByLabelText(text: String) = screen.getAllByLabelText(text)
    suspend fun queryAllByLabelText(text: String) = screen.queryAllByLabelText(text)
    suspend fun findAllByLabelText(text: String) = screen.findAllByLabelText(text).await()
}
