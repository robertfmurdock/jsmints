package com.zegreatrob.testmints.action

interface ExecutableAction<in T, R> : DispatchableAction<T, R> {
    fun execute(dispatcher: T): R
}
