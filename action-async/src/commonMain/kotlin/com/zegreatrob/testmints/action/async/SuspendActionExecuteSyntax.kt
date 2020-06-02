package com.zegreatrob.testmints.action.async

interface SuspendActionExecuteSyntax {
    suspend fun <D, R> D.execute(action: SuspendAction<D, R>): R = action.execute(this)
}

suspend fun <D : SuspendActionExecuteSyntax, R> D.execute(action: SuspendAction<D, R>) = execute(action)
