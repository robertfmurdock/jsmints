package com.zegreatrob.testmints

object StandardMints : StandardMintDispatcher {
    override var reporter: MintReporter = object : MintReporter {}
}

fun <C : Any> setup(context: C) = StandardMints.setup(context)