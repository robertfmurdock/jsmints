package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.RenderOptions
import com.zegreatrob.wrapper.testinglibrary.react.external.Result
import react.ChildrenBuilder
import react.Fragment
import react.ReactNode
import react.create

interface TestingLibraryFunctions {
    fun render(node: ReactNode): Result
    fun render(node: ReactNode, options: RenderOptions): Result
}

fun TestingLibraryFunctions.render(block: ChildrenBuilder.() -> Unit) = render(Fragment.create(block))
fun TestingLibraryFunctions.render(options: RenderOptions, block: ChildrenBuilder.() -> Unit) =
    render(Fragment.create(block), options)
