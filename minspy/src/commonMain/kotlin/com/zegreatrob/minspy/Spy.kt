package com.zegreatrob.minspy

import kotlin.test.fail

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

fun Spy<Unit, Unit>.spyFunction() {
    spyReceivedValues.add(Unit)
}

fun <O> Spy<Unit, O>.spyFunction() = spyFunction(Unit)

fun <I> Spy<I, Unit>.spyFunction(input: I) {
    spyReceivedValues.add(input)
}

fun <I, O> Spy<I, O>.spyFunction(input: I): O = spyReturnWhenGivenValues.fixedGetOrElse(input) {
    safePop(input)
}.also { spyReceivedValues.add(input) }

private fun <I, O> Spy<I, O>.safePop(input: I): O = if (spyReturnValues.size > 0)
    spyReturnValues[0].also { spyReturnValues.removeAt(0) }
else
    fail("No values remaining given input $input")


private fun <K, V> Map<K, V>.fixedGetOrElse(input: K, function: () -> V) = if (containsKey(input)) {
    getValue(input)
} else {
    function()
}

class SpyData<I, O> : Spy<I, O> {
    override val spyReturnWhenGivenValues = mutableMapOf<I, O>()
    override val spyReceivedValues = mutableListOf<I>()
    override val spyReturnValues = mutableListOf<O>()
}
