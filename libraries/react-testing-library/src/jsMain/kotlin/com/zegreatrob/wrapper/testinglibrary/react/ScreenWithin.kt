package com.zegreatrob.wrapper.testinglibrary.react

import org.w3c.dom.Element

internal object ScreenWithin : Within {
    override fun within(element: Element?) = WithinQueries(element)
}
