package com.zegreatrob.testmints

interface MintReporter {
    fun exerciseStart(context: Any) = Unit
    fun exerciseFinish() = Unit
    fun verifyStart(payload: Any?) = Unit
    fun verifyFinish() = Unit
}