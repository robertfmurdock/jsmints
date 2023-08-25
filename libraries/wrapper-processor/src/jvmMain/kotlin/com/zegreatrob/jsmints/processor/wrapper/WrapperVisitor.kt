package com.zegreatrob.jsmints.processor.wrapper

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.visitor.KSTopDownVisitor

class WrapperVisitor(private val logger: KSPLogger) : KSTopDownVisitor<CodeGenerator, Unit>() {
    override fun defaultHandler(node: KSNode, data: CodeGenerator) {
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: CodeGenerator) {
        super.visitClassDeclaration(classDeclaration, data)
    }

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: CodeGenerator) {
        super.visitPropertyDeclaration(property, data)
    }

}
