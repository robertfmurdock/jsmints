package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.Screen
import com.zegreatrob.wrapper.testinglibrary.react.external.TestingLibraryRoleOptions
import js.core.jso
import kotlinx.coroutines.await

interface ScreenByRole : ByRole {

    val screen: Screen

    override fun getByRole(role: String, options: RoleOptions) =
        screen.getByRole(role, toTestingLibraryOptions(options))

    override suspend fun findByRole(role: String, options: RoleOptions) =
        screen.findByRole(role, toTestingLibraryOptions(options)).await()

    override fun queryByRole(role: String, options: RoleOptions) =
        screen.queryByRole(role, toTestingLibraryOptions(options))

    override fun getAllByRole(role: String, options: RoleOptions) =
        screen.getAllByRole(role, toTestingLibraryOptions(options))

    override fun queryAllByRole(role: String, options: RoleOptions) =
        screen.queryAllByRole(role, toTestingLibraryOptions(options))

    override suspend fun findAllByRole(role: String, options: RoleOptions) =
        screen.findAllByRole(role, toTestingLibraryOptions(options)).await()

    private fun toTestingLibraryOptions(options: RoleOptions): TestingLibraryRoleOptions = jso {
        options.name?.let { this.name = it }
        options.selected?.let { this.selected = it }
    }
}
