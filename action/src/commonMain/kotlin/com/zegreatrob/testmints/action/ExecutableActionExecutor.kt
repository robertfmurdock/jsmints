package com.zegreatrob.testmints.action

interface ExecutableActionExecutor<out D> {
    operator fun <R> invoke(action: ExecutableAction<D, R>): R
}
