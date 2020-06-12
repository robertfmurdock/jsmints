package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.MintReporterConfig
import com.zegreatrob.testmints.report.ReporterProvider

object StandardMints : StandardMintDispatcher, ReporterProvider by MintReporterConfig

fun <C : Any> setup(context: C, additionalSetupActions: C.() -> Unit = {}) =
        StandardMints.setup(context, additionalSetupActions)

fun testTemplate(sharedSetup: () -> Unit, sharedTeardown: () -> Unit) =
        StandardMints.testTemplate(sharedSetup, sharedTeardown)