package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.RenderOptions
import com.zegreatrob.wrapper.testinglibrary.react.external.Result
import react.ReactNode

interface TestingLibraryFunctions {
    fun render(node: ReactNode): Result
    fun render(node: ReactNode, options: RenderOptions): Result
}
