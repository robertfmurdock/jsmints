package com.zegreatrob.minspy

import kotlin.test.fail

interface Spy<I, O> {
    val spyReceivedValues: MutableList<I>
    val spyReturnValues: MutableList<O>

    val spyReturnWhenGivenValues: MutableMap<I, O>

    fun spyFunction(input: I): O = when (val value = spyReturnWhenGivenValues[input]) {
        null -> safePop(input)
        else -> value
    }.also { spyReceivedValues.add(input) }

    private fun safePop(input: I): O {
        val value = spyReturnValues.popValue()
        if (value == null) {
            fail("No values remaining given input $input")
        } else {
            return value
        }
    }

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

class SpyData<I, O> : Spy<I, O> {
    override val spyReturnWhenGivenValues = mutableMapOf<I, O>()
    override val spyReceivedValues = mutableListOf<I>()
    override val spyReturnValues = mutableListOf<O>()
}

fun <T> MutableList<T>.popValue() = getOrNull(0)?.also { removeAt(0) }