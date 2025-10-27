package com.zegreatrob.jsmints.processor.wrapper

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import com.squareup.kotlinpoet.ksp.writeTo

class WrapperVisitor(private val logger: KSPLogger) : KSTopDownVisitor<CodeGenerator, Unit>() {
    override fun defaultHandler(node: KSNode, data: CodeGenerator) {
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: CodeGenerator) {
        super.visitClassDeclaration(classDeclaration, data)
        if (
            classDeclaration.classKind == ClassKind.INTERFACE
            && classDeclaration.modifiers.containsAll(listOf(Modifier.SEALED, Modifier.EXTERNAL))
        ) {
            val resolver = classDeclaration.typeParameters.toTypeParameterResolver()

            val builder = FileSpec.builder(classDeclaration.packageName.asString(), "${classDeclaration}Extentions")
            val propsClassName = parameterizedClassName(classDeclaration)
            addComponentFunctions(classDeclaration, resolver, propsClassName, builder)
            builder.build()
                .writeTo(
                    data, Dependencies(
                        aggregating = false,
                        sources = setOfNotNull(classDeclaration.containingFile).toTypedArray()
                    )
                )
        }
    }

    private fun addComponentFunctions(
        classDeclaration: KSClassDeclaration,
        resolver: TypeParameterResolver,
        propsClassName: TypeName,
        builder: FileSpec.Builder
    ) {
        val functions = classDeclaration.getAllProperties().mapIndexed { index, value ->
            val component = "component${index + 1}"
            FunSpec.builder(component)
                .addTypeVariables(classDeclaration.typeParameters.map { it.toTypeVariableName(resolver) })
                .receiver(propsClassName)
                .addModifiers(KModifier.OPERATOR)
                .returns(value.type.toTypeName(resolver))
                .addCode("return ${value.simpleName.asString()}")
                .build()
        }
        functions.forEach { builder.addFunction(it) }
    }

}

private fun parameterizedClassName(classDeclaration: KSClassDeclaration) = classDeclaration.toClassName().let {
    if (classDeclaration.typeParameters.isEmpty()) {
        it
    } else {
        it.parameterizedBy(classDeclaration.typeParameters.map { it.toTypeVariableName() })
    }
}
