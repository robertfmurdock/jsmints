package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.MintReporterConfig
import com.zegreatrob.testmints.report.ReporterProvider

object StandardMints : StandardMintDispatcher, ReporterProvider by MintReporterConfig

fun <C : Any> setup(context: C) = StandardMints.setup(context)
