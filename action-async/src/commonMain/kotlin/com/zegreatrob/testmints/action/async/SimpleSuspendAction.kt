package com.zegreatrob.testmints.action.async

interface SimpleSuspendAction<T, R> : SuspendAction<T, R> {
    override suspend fun execute(dispatcher: T) = performFunc(dispatcher)
    val performFunc: suspend (T) -> R
    fun <A> A.link(performFunc: suspend (T, A) -> R): suspend (T) -> R = { performFunc(it, this) }
}
