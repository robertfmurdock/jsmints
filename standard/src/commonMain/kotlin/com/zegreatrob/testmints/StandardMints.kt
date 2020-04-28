package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.MintReporter

object StandardMints : StandardMintDispatcher {
    override var reporter: MintReporter = object : MintReporter {}
}

fun <C : Any> setup(context: C) = StandardMints.setup(context)