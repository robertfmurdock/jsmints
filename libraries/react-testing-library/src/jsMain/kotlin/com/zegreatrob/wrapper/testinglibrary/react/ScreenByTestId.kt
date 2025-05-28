package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.Screen
import kotlinx.coroutines.await

interface ScreenByTestId : ByTestId {

    val screen: Screen

    override fun getByTestId(text: String) = screen.getByTestId(text)
    override fun getAllByTestId(text: String) = screen.getAllByTestId(text)
    override fun queryByTestId(text: String) = screen.queryByTestId(text)
    override fun queryAllByTestId(text: String) = screen.queryAllByTestId(text)
    override suspend fun findByTestId(text: String) = screen.findByTestId(text).await()
    override suspend fun findAllByTestId(text: String) = screen.findAllByTestId(text).await()
}
