package com.zegreatrob.testmints.action.async

@Suppress("unused")
interface SuspendActionExecutor<out D> {
    suspend operator fun <R> invoke(action: SuspendAction<D, R>): R
}
