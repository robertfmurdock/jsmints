package com.zegreatrob.jsmints.processor.wrapper

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class WrapperProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) =
        WrapperProcessor(environment.codeGenerator, environment.logger)
}