package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.reactTestingLibrary
import kotlinx.coroutines.await

object TestingLibraryReact : TestingLibraryFunctions by ScreenTestingLibraryFunctions, Within by ScreenWithin {
    val screen = object : TestingLibraryQueries by ScreenTestingLibraryQueries {}

    suspend fun <T : Any> waitFor(callback: () -> T?): Unit = reactTestingLibrary.waitFor(callback).await()

    fun act(block: () -> Unit) = reactTestingLibrary.act(block)
    val fireEvent = reactTestingLibrary.fireEvent
}
