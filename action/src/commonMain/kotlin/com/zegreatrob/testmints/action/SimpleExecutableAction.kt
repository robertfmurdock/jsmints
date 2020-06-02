package com.zegreatrob.testmints.action

interface SimpleExecutableAction<T, R> : ExecutableAction<T, R> {
    override fun execute(dispatcher: T) = performFunc(dispatcher)
    val performFunc: (T) -> R
    fun <A> A.link(performFunc: (T, A) -> R): (T) -> R = { performFunc(it, this) }
}
