package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.reactTestingLibrary

interface ScreenTestingLibraryQueries :
    TestingLibraryQueries,
    ScreenByRole,
    ScreenByText,
    ScreenByLabelText,
    ScreenWithin {
    companion object : ScreenTestingLibraryQueries {
        override val screen = reactTestingLibrary.screen
    }
}
