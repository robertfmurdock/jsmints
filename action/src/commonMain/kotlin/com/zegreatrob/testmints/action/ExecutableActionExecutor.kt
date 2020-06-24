package com.zegreatrob.testmints.action

@Suppress("unused")
interface ExecutableActionExecutor<out D> {
    operator fun <R> invoke(action: ExecutableAction<D, R>): R
}
