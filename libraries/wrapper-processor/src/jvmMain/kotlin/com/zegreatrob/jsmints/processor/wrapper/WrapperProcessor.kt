package com.zegreatrob.jsmints.processor.wrapper

import com.google.devtools.ksp.buildApiFeatures
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated

class WrapperProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
    override val apiFeatures = buildApiFeatures { enableBackingFields = true }

    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }
        invoked = true

        val visitor = WrapperVisitor(logger, apiFeatures)
        resolver.getAllFiles().forEach {
            it.accept(visitor, codeGenerator)
        }
        return emptyList()
    }
}
