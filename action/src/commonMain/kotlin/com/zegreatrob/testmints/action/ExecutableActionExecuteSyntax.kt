package com.zegreatrob.testmints.action

interface ExecutableActionExecuteSyntax {
    fun <D, R> D.execute(action: ExecutableAction<D, R>): R = action.execute(this)
}
