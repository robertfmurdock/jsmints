package com.zegreatrob.wrapper.testinglibrary.react

import org.w3c.dom.HTMLElement

interface Within {
    suspend fun within(element: HTMLElement): TestingLibraryQueries
}
