package com.zegreatrob.wrapper.testinglibrary.react

import org.w3c.dom.HTMLElement

interface ByRole {
    suspend fun getByRole(role: String, options: RoleOptions = RoleOptions(selected = true)): HTMLElement

    suspend fun findByRole(role: String, options: RoleOptions = RoleOptions(selected = true)): HTMLElement

    suspend fun queryByRole(role: String, options: RoleOptions = RoleOptions(selected = true)): HTMLElement?

    suspend fun getAllByRole(role: String, options: RoleOptions = RoleOptions(selected = true)): Array<HTMLElement>

    suspend fun queryAllByRole(role: String, options: RoleOptions = RoleOptions(selected = true)): Array<HTMLElement>

    suspend fun findAllByRole(role: String, options: RoleOptions = RoleOptions(selected = true)): Array<HTMLElement>
}
