package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.MintReporterConfig
import com.zegreatrob.testmints.report.ReporterProvider
import kotlin.jvm.JvmName

object StandardMints : StandardMintDispatcher, ReporterProvider by MintReporterConfig

fun <C : Any> setup(context: C, additionalSetupActions: C.() -> Unit = {}) =
        StandardMints.setup(context, additionalSetupActions)

fun <SC : Any> testTemplate(sharedSetup: () -> SC, sharedTeardown: (SC) -> Unit = {}) =
        StandardMints.testTemplate(sharedSetup, sharedTeardown)

fun testTemplate(sharedSetup: () -> Unit, sharedTeardown: () -> Unit) =
        StandardMints.testTemplate(sharedSetup, { sharedTeardown() })

@JvmName("testTemplateSimple")
fun testTemplate(wrapper: SimpleWrapper) = StandardMints.testTemplateSimple(wrapper)

@JvmName("testTemplateSC")
fun <SC: Any> testTemplate(wrapper: Wrapper<SC>) = StandardMints.testTemplate(wrapper)

typealias SimpleWrapper = (() -> Unit) -> Unit
typealias Wrapper<SC> = ((SC) -> Unit) -> Unit
