package com.zegreatrob.testmints.report

interface MintReporter {
    fun exerciseStart(context: Any) = Unit
    fun exerciseFinish() = Unit
    fun verifyStart(payload: Any?) = Unit
    fun verifyFinish() = Unit
    fun teardownStart() = Unit
    fun teardownFinish() = Unit
}
