package com.zegreatrob.testmints

interface MintReporter {
    fun exerciseStart() = Unit
    fun exerciseFinish() = Unit
    fun verifyStart() = Unit
    fun verifyFinish() = Unit
}