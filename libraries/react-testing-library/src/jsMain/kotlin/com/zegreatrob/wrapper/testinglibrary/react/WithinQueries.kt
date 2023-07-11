package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.Screen
import com.zegreatrob.wrapper.testinglibrary.react.external.reactTestingLibrary
import org.w3c.dom.Element

class WithinQueries(element: Element?) : ScreenTestingLibraryQueries {
    override val screen: Screen = reactTestingLibrary.within(element)
}
