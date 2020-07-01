package com.zegreatrob.minspy

interface Spy<I, O> {
    val spyReceivedValues: MutableList<I>

    val callCount: Int get() = spyReceivedValues.size

    val spyReturnValues: MutableList<O>

    val spyReturnWhenGivenValues: MutableMap<I, O>


    @Suppress("unused")
    infix fun spyWillReturn(values: Collection<O>) {
        spyReturnValues += values
    }

    infix fun spyWillReturn(value: O) {
        spyReturnValues += value
    }

    fun whenever(receive: I, returnValue: O) {
        spyReturnWhenGivenValues[receive] = returnValue
    }

    fun cancel(): Nothing = throw NotImplementedError("Will not implement unused collaborator")
}
