package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.reactTestingLibrary

internal interface ScreenTestingLibraryQueries :
    TestingLibraryQueries,
    ScreenByRole,
    ScreenByText,
    ScreenByLabelText,
    ScreenByAltText,
    ScreenByTestId {
    companion object : ScreenTestingLibraryQueries {
        override val screen = reactTestingLibrary.screen
    }
}
