package com.zegreatrob.jsmints.processor.minreact

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class MinreactProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) =
        MinreactProcessor(environment.codeGenerator, environment.logger)
}