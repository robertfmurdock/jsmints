package com.zegreatrob.minspy

import kotlin.test.fail

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