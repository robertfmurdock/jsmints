package com.zegreatrob.wrapper.testinglibrary.react

import org.w3c.dom.HTMLElement

interface ByRole {
    fun getByRole(role: String, options: RoleOptions = RoleOptions()): HTMLElement
    fun getAllByRole(role: String, options: RoleOptions = RoleOptions()): Array<HTMLElement>
    fun queryByRole(role: String, options: RoleOptions = RoleOptions()): HTMLElement?
    fun queryAllByRole(role: String, options: RoleOptions = RoleOptions()): Array<HTMLElement>
    suspend fun findByRole(role: String, options: RoleOptions = RoleOptions()): HTMLElement
    suspend fun findAllByRole(role: String, options: RoleOptions = RoleOptions()): Array<HTMLElement>
}
