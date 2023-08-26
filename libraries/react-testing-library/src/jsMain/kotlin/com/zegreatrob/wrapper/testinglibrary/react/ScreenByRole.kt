package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.wrapper.testinglibrary.react.external.Screen
import kotlinx.coroutines.await

interface ScreenByRole : ByRole {

    val screen: Screen

    override fun getByRole(role: String, options: RoleOptions) =
        screen.getByRole(role, options)

    override suspend fun findByRole(role: String, options: RoleOptions) =
        screen.findByRole(role, options).await()

    override fun queryByRole(role: String, options: RoleOptions) =
        screen.queryByRole(role, options)

    override fun getAllByRole(role: String, options: RoleOptions) =
        screen.getAllByRole(role, options)

    override fun queryAllByRole(role: String, options: RoleOptions) =
        screen.queryAllByRole(role, options)

    override suspend fun findAllByRole(role: String, options: RoleOptions) =
        screen.findAllByRole(role, options).await()
}
