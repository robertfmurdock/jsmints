package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.RenderOptions
import com.zegreatrob.wrapper.testinglibrary.react.external.reactTestingLibrary
import react.ChildrenBuilder
import react.Fragment
import react.ReactNode
import react.create

internal object ScreenTestingLibraryFunctions : TestingLibraryFunctions {
    override fun render(node: ReactNode) = reactTestingLibrary.render(node)
    override fun render(node: ReactNode, options: RenderOptions) = reactTestingLibrary.render(node, options)
    override fun render(block: ChildrenBuilder.() -> Unit) = render(Fragment.create(block))
    override fun render(options: RenderOptions, block: ChildrenBuilder.() -> Unit) =
        render(Fragment.create(block), options)
}
