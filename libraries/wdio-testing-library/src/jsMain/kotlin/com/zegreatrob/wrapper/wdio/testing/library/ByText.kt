package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import com.zegreatrob.wrapper.wdio.browser
import kotlinx.coroutines.await

interface ByText : BrowserProvider {
    suspend fun getByText(text: String) = WebdriverElement(finder = { extendedWdioBrowser.getByText(text).await() })
        .apply { waitToExist() }

    suspend fun findByText(text: String) = WebdriverElement(finder = { extendedWdioBrowser.findByText(text).await() })
        .apply { waitToExist() }

    suspend fun queryByText(text: String) = WebdriverElement(finder = {
        extendedWdioBrowser.queryByText(text).await()
            ?: browser.`$`("element-with-text-$text-not-found").await()
    })

    suspend fun getAllByText(text: String) = WebdriverElementArray(finder = {
        extendedWdioBrowser.getAllByText(text).await()
            .map { WebdriverElement(finder = { it }) }
    })

    suspend fun queryAllByText(text: String) = WebdriverElementArray(finder = {
        extendedWdioBrowser.queryAllByText(text)
            .await().map { WebdriverElement(finder = { it }) }
    })

    suspend fun findAllByText(text: String) = WebdriverElementArray(finder = {
        extendedWdioBrowser.findAllByText(text)
            .await().map { WebdriverElement(finder = { it }) }
    })
}
