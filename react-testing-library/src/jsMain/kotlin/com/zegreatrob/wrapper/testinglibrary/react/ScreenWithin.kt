package com.zegreatrob.wrapper.testinglibrary.react

import org.w3c.dom.HTMLElement

internal object ScreenWithin : Within {
    override fun within(element: HTMLElement) = WithinQueries(element)
}
