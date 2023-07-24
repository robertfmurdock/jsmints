package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.reactTestingLibrary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlin.coroutines.coroutineContext

object TestingLibraryReact : TestingLibraryFunctions by ScreenTestingLibraryFunctions, Within by ScreenWithin {
    val screen = object : TestingLibraryQueries by ScreenTestingLibraryQueries {}

    suspend fun <T : Any> waitFor(callback: () -> T?): Unit = reactTestingLibrary.waitFor(callback).await()

    suspend fun <R> act(block: suspend () -> R) = CoroutineScope(coroutineContext).run {
        reactTestingLibrary.act { promise { block() } }.await()
    }

    val fireEvent = reactTestingLibrary.fireEvent
}
