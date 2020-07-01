package com.zegreatrob.minspy

class SpyData<I, O> : Spy<I, O> {
    override val spyReturnWhenGivenValues = mutableMapOf<I, O>()
    override val spyReceivedValues = mutableListOf<I>()
    override val spyReturnValues = mutableListOf<O>()
}
