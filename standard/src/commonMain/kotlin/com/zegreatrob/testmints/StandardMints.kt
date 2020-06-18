package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.MintReporterConfig
import com.zegreatrob.testmints.report.ReporterProvider

object StandardMints : StandardMintDispatcher, ReporterProvider by MintReporterConfig

fun <C : Any> setup(context: C, additionalSetupActions: C.() -> Unit = {}) =
        StandardMints.setup(context, additionalSetupActions)

fun <SC : Any> testTemplate(sharedSetup: () -> SC, sharedTeardown: (SC) -> Unit) =
        StandardMints.testTemplate(sharedSetup, sharedTeardown)

fun testTemplate(sharedSetup: () -> Unit, sharedTeardown: () -> Unit) =
        StandardMints.testTemplate(sharedSetup, { sharedTeardown() })

fun testTemplate(wrapper: (() -> Unit) -> Unit) = StandardMints.testTemplate(wrapper)