package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import com.zegreatrob.wrapper.wdio.browser
import com.zegreatrob.wrapper.wdio.testing.library.external.RoleOptions
import kotlinx.coroutines.await
import kotlinx.js.jso

interface ByRole : BrowserProvider {
    suspend fun getByRole(role: String, name: String? = null) =
        WebdriverElement(finder = { extendedWdioBrowser.getByRole(role, roleOptions(name)).await() })
            .apply { waitToExist() }

    suspend fun findByRole(role: String, name: String? = null) =
        WebdriverElement(finder = { extendedWdioBrowser.findByRole(role, roleOptions(name)).await() })
            .apply { waitToExist() }

    suspend fun queryByRole(role: String, name: String? = null) = WebdriverElement(finder = {
        extendedWdioBrowser.queryByRole(role, roleOptions(name)).await()
            ?: browser.`$`("element-with-role-$role-not-found").await()
    })

    fun roleOptions(name: String?): RoleOptions = jso {
        name?.let { this.name = it }
    }

    suspend fun getAllByRole(role: String, name: String? = null) = WebdriverElementArray(finder = {
        extendedWdioBrowser.getAllByRole(role, roleOptions(name)).await()
            .map { WebdriverElement(finder = { it }) }
    })

    suspend fun queryAllByRole(role: String, name: String? = null) = WebdriverElementArray(finder = {
        extendedWdioBrowser.queryAllByRole(role, roleOptions(name))
            .await().map { WebdriverElement(finder = { it }) }
    })

    suspend fun findAllByRole(role: String, name: String? = null) = WebdriverElementArray(finder = {
        extendedWdioBrowser.findAllByRole(role, roleOptions(name))
            .await().map { WebdriverElement(finder = { it }) }
    })
}
