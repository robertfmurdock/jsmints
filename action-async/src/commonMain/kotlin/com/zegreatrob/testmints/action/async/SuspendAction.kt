package com.zegreatrob.testmints.action.async

import com.zegreatrob.testmints.action.DispatchableAction

interface SuspendAction<in T, R> : DispatchableAction<T, R> {
    suspend fun execute(dispatcher: T): R
}
