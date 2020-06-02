package com.zegreatrob.testmints.action.async

interface GeneralSuspendActionDispatcherSyntax : SuspendActionExecuteSyntax {
    val generalDispatcher: GeneralSuspendActionDispatcher
    override suspend fun <D, R> D.execute(action: SuspendAction<D, R>) = generalDispatcher.dispatch(action, this)
}
