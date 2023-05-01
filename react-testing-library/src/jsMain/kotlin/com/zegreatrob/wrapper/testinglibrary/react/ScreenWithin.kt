package com.zegreatrob.wrapper.testinglibrary.react

import org.w3c.dom.HTMLElement

interface ScreenWithin : Within {
    override suspend fun within(element: HTMLElement) = WithinQueries(element)
}
