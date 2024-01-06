package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import com.zegreatrob.wrapper.wdio.browser
import js.objects.jso
import kotlinx.coroutines.await

interface ByRole : BrowserProvider {
    suspend fun getByRole(role: String, options: RoleOptions = RoleOptions()) =
        WebdriverElement(finder = { extendedWdioBrowser.getByRole(role, toTestingLibraryOptions(options)).await() })
            .apply { waitToExist() }

    suspend fun findByRole(role: String, options: RoleOptions = RoleOptions()) =
        WebdriverElement(finder = { extendedWdioBrowser.findByRole(role, toTestingLibraryOptions(options)).await() })
            .apply { waitToExist() }

    suspend fun queryByRole(role: String, options: RoleOptions = RoleOptions()) = WebdriverElement(finder = {
        extendedWdioBrowser.queryByRole(role, toTestingLibraryOptions(options)).await()
            ?: browser.`$`("element-with-role-$role-not-found").await()
    })

    suspend fun getAllByRole(role: String, options: RoleOptions = RoleOptions()) = WebdriverElementArray(finder = {
        extendedWdioBrowser.getAllByRole(role, toTestingLibraryOptions(options)).await()
            .map { WebdriverElement(finder = { it }) }
    })

    suspend fun queryAllByRole(role: String, options: RoleOptions = RoleOptions()) = WebdriverElementArray(finder = {
        extendedWdioBrowser.queryAllByRole(role, toTestingLibraryOptions(options))
            .await().map { WebdriverElement(finder = { it }) }
    })

    suspend fun findAllByRole(role: String, options: RoleOptions = RoleOptions()) = WebdriverElementArray(finder = {
        extendedWdioBrowser.findAllByRole(role, toTestingLibraryOptions(options))
            .await().map { WebdriverElement(finder = { it }) }
    })

    private fun toTestingLibraryOptions(options: RoleOptions): RoleOptions = jso {
        options.name?.let { this.name = it }
    }
}
