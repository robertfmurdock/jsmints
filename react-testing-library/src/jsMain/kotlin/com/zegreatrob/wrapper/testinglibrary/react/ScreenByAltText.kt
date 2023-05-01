package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.Screen
import kotlinx.coroutines.await

interface ScreenByAltText : ByAltText {

    val screen: Screen

    override fun getByAltText(text: String) = screen.getByAltText(text)
    override fun getAllByAltText(text: String) = screen.getAllByAltText(text)
    override fun queryByAltText(text: String) = screen.queryByAltText(text)
    override fun queryAllByAltText(text: String) = screen.queryAllByAltText(text)
    override suspend fun findByAltText(text: String) = screen.findByAltText(text).await()
    override suspend fun findAllByAltText(text: String) = screen.findAllByAltText(text).await()
}
