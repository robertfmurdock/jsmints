package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.RenderOptions
import com.zegreatrob.wrapper.testinglibrary.react.external.Result
import react.ChildrenBuilder
import react.ReactDsl
import react.ReactNode

interface TestingLibraryFunctions {
    fun render(node: ReactNode): Result
    fun render(node: ReactNode, options: RenderOptions): Result
    fun render(block: @ReactDsl ChildrenBuilder.() -> Unit): Result
    fun render(options: RenderOptions, block: @ReactDsl ChildrenBuilder.() -> Unit): Result
}
