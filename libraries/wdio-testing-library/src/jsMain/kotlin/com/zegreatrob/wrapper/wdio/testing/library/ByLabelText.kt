package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import com.zegreatrob.wrapper.wdio.browser
import kotlinx.coroutines.await

interface ByLabelText : BrowserProvider {
    suspend fun getByLabelText(text: String) = WebdriverElement(finder = { extendedWdioBrowser.getByLabelText(text).await() })
        .apply { waitToExist() }

    suspend fun findByLabelText(text: String) = WebdriverElement(finder = { extendedWdioBrowser.findByLabelText(text).await() })
        .apply { waitToExist() }

    suspend fun queryByLabelText(text: String) = WebdriverElement(finder = {
        extendedWdioBrowser.queryByLabelText(text).await()
            ?: browser.`$`("element-with-text-$text-not-found").await()
    })

    suspend fun getAllByLabelText(text: String) = WebdriverElementArray(finder = {
        extendedWdioBrowser.getAllByLabelText(text).await()
            .map { WebdriverElement(finder = { it }) }
    })

    suspend fun queryAllByLabelText(text: String) = WebdriverElementArray(finder = {
        extendedWdioBrowser.queryAllByLabelText(text)
            .await().map { WebdriverElement(finder = { it }) }
    })

    suspend fun findAllByLabelText(text: String) = WebdriverElementArray(finder = {
        extendedWdioBrowser.findAllByLabelText(text)
            .await().map { WebdriverElement(finder = { it }) }
    })
}
