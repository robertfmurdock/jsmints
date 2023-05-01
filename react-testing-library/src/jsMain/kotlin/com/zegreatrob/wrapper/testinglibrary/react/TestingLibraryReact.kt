package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.Screen
import com.zegreatrob.wrapper.testinglibrary.react.external.reactTestingLibrary
import org.w3c.dom.HTMLElement

object TestingLibraryReact : TestingLibraryQueries {
    override val screen: Screen = reactTestingLibrary.screen
}

interface TestingLibraryQueries : ByRole, ByText, ByLabelText, Within

interface Within {
    suspend fun within(element: HTMLElement) = object : TestingLibraryQueries {
        override val screen: Screen get() = reactTestingLibrary.within(element)
    }
}
