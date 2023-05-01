package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.RenderOptions
import com.zegreatrob.wrapper.testinglibrary.react.external.reactTestingLibrary
import react.ReactNode

internal object ScreenTestingLibraryFunctions : TestingLibraryFunctions {
    override fun render(node: ReactNode) = reactTestingLibrary.render(node)
    override fun render(node: ReactNode, options: RenderOptions) = reactTestingLibrary.render(node, options)
}
