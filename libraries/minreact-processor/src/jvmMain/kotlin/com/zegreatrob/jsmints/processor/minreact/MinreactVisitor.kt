package com.zegreatrob.jsmints.processor.minreact

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSCallableReference
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import com.squareup.kotlinpoet.ksp.writeTo

class MinreactVisitor(private val logger: KSPLogger) : KSTopDownVisitor<CodeGenerator, Unit>() {
    override fun defaultHandler(node: KSNode, data: CodeGenerator) {
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: CodeGenerator) {
        super.visitClassDeclaration(classDeclaration, data)
        if (classDeclaration.extends(ClassName("react", "Props"))) {
            val resolver = classDeclaration.typeParameters.toTypeParameterResolver()

            val builder = FileSpec.builder(classDeclaration.packageName.asString(), "${classDeclaration}Extentions")
            builder.addImport("react", "create")
            val propsClassName = parameterizedClassName(classDeclaration)
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

            var paramsAssignments = propertiesAsParameterAssignments(classDeclaration, resolver)

            var bodyArgs = listOf<Any?>(MemberName("react", "create"))
            val codeBlock = CodeBlock.of(
                """
                    | return %M {
                    |       $paramsAssignments
                    |       }
                """.trimMargin(),
                args = bodyArgs.toTypedArray()
            )
            builder.addFunction(
                FunSpec.builder("create")
                    .addTypeVariables(classDeclaration.typeParameters.map { it.toTypeVariableName(resolver) })
                    .addParameters(parameterSpecs(classDeclaration, resolver))
                    .receiver(ClassName("react", "ElementType").parameterizedBy(propsClassName))
                    .returns(ClassName("react", "ReactNode"))
                    .addCode(codeBlock)
                    .build()
            )
            builder.build()
                .writeTo(
                    data, Dependencies(
                        aggregating = false,
                        sources = setOfNotNull(classDeclaration.containingFile).toTypedArray()
                    )
                )
        }
    }

    private fun KSClassDeclaration.extends(className: ClassName) = getAllSuperTypes()
        .mapNotNull { this@MinreactVisitor.runCatching { it.toClassName() }.getOrNull() }
        .contains(className)

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: CodeGenerator) {
        super.visitPropertyDeclaration(property, data)
        val targetName = property.simpleName.getShortName()

        if (property.annotations.any(::isMinreact)) {
            val resolvedType: KSType = property.type.resolve()

            resolvedType.arguments.forEach { argument ->
                val propsType = argument.type?.resolve()
                val classDeclaration = propsType?.declaration as? KSClassDeclaration
                    ?: return@forEach
                val resolver = classDeclaration.typeParameters.toTypeParameterResolver()
                var paramsAssignments = propertiesAsParameterAssignments(classDeclaration, resolver)

                var bodyArgs = listOf<Any?>(parameterizedTypeName(classDeclaration))

                val body = """
                    |  val component = ($targetName.unsafeCast<%T>())
                    |  component {
                    |       $paramsAssignments
                    |       }
                """.trimMargin()



                FileSpec.builder(property.packageName.asString(), "${targetName}Kt")
                    .addImport("react", "create")
                    .addFunction(
                        builderFunction(
                            ClassName("react", "ChildrenBuilder"),
                            targetName,
                            classDeclaration,
                            resolver,
                            body,
                            bodyArgs
                        )
                    )
                    .build()
                    .writeTo(
                        data, Dependencies(
                            aggregating = false,
                            sources = setOfNotNull(property.containingFile, classDeclaration.containingFile)
                                .toTypedArray()
                        )
                    )
            }
        }
    }

    private fun propertiesAsParameterAssignments(classDeclaration: KSClassDeclaration, resolver: TypeParameterResolver) =
        classDeclaration.getAllProperties()
            .joinToString("\n", transform = { property -> assignPropByParameter(property, resolver) })

    private fun builderFunction(
        receiver: ClassName,
        targetName: String,
        classDeclaration: KSClassDeclaration,
        resolver: TypeParameterResolver,
        body: String,
        bodyArgs: List<Any?>
    ) = FunSpec.builder(targetName)
        .addTypeVariables(classDeclaration.typeParameters.map { it.toTypeVariableName(resolver) })
        .addParameters(parameterSpecs(classDeclaration, resolver))
        .receiver(receiver)
        .returns(Unit::class)
        .addCode(CodeBlock.of(body, args = bodyArgs.toTypedArray()))
        .build()

    private fun parameterizedTypeName(classDeclaration: KSClassDeclaration): ParameterizedTypeName {
        val propsType = parameterizedClassName(classDeclaration)
        return ClassName("react", "FC").parameterizedBy(propsType)
    }

    private fun parameterizedClassName(classDeclaration: KSClassDeclaration) = classDeclaration.toClassName().let {
        if (classDeclaration.typeParameters.isEmpty()) {
            it
        } else {
            it.parameterizedBy(classDeclaration.typeParameters.map { it.toTypeVariableName() })
        }
    }

    private fun assignPropByParameter(property: KSPropertyDeclaration, resolver: TypeParameterResolver) = when {
        isChildrenNode(property) -> childrenBuilderFunction(property, resolver)
        property.type.resolve().isMarkedNullable ->
            "${property.simpleName.getShortName()}?.let { this.${property.simpleName.getShortName()} = it }"

        else -> "this.${property.simpleName.getShortName()} = ${property.simpleName.getShortName()}"
    }

    private fun childrenBuilderFunction(declaration: KSPropertyDeclaration, resolver: TypeParameterResolver): String {
        val callableRef = declaration.toCallableRef()
            ?: return CodeBlock.of(
                format = "this.children = %T.create(block = { children() })",
                args = arrayOf(ClassName("react", "Fragment"))
            ).toString()
        val parameters = callableRef.parametersOfFunctionType(resolver)
        val childrenFunc = FunSpec.builder("childrenFunc")
            .addParameters(
                parameters
                    .mapIndexed { index, it -> ParameterSpec.builder("cp$index", it).build() }
            )
            .returns(callableRef.returnType.resolve().toTypeName())
            .addCode(
                CodeBlock.of(
                    format = """
                        return %T.create(block = { children(""".trimIndent() + (0..<parameters.size).joinToString(
                        ","
                    ) { "cp$it" } + ") })",
                    args = arrayOf(ClassName("react", "Fragment"))
                )
            )
            .build()
            .toString()
            .replace("public fun", "fun")
            .replace("childrenFunc", "")
        return "this.children = ${childrenFunc};"
    }

    private fun parameterSpecs(
        classDeclaration: KSClassDeclaration,
        resolver: TypeParameterResolver
    ): Iterable<ParameterSpec> {
        val childrenNode = classDeclaration.getAllProperties().find(::isChildrenNode)

        return classDeclaration.getAllProperties()
            .filterNot(::isChildrenNode)
            .map { parameterSpec(it, resolver) }
            .let {
                if (childrenNode == null) it else {
                    it + childrenParameter(childrenNode, resolver)
                }
            }
            .asIterable()
    }

    private fun childrenParameter(childrenNode: KSPropertyDeclaration, resolver: TypeParameterResolver): ParameterSpec {
        val callableRef = childrenNode.toCallableRef()
        val parameters = callableRef?.parametersOfFunctionType(resolver) ?: emptyArray()
        return ParameterSpec.builder(
            "children", LambdaTypeName.get(
                receiver = ClassName("react", "ChildrenBuilder"),
                parameters = parameters,
                returnType = UNIT
            )
        )
            .defaultValue("{ ${ (0..<parameters.size).joinToString(","){"_"}  } -> }")
            .build()
    }

    private fun KSCallableReference.parametersOfFunctionType(resolver: TypeParameterResolver): Array<TypeName> =
        functionParameters.map { it.type.toTypeName(resolver) }
            .toTypedArray()

    private fun KSPropertyDeclaration.toCallableRef(): KSCallableReference? =
        (type.element as? KSCallableReference)

    private fun isChildrenNode(it: KSPropertyDeclaration) = it.simpleName.asString() == "children"

    private fun parameterSpec(
        it: KSPropertyDeclaration,
        resolver: TypeParameterResolver
    ): ParameterSpec {
        val typeName = it.type.toTypeName(resolver)
        val builder = ParameterSpec.Companion.builder(it.simpleName.getShortName(), typeName)

        if (typeName.isNullable) {
            builder.defaultValue(CodeBlock.of("null"))
        }

        return builder
            .build()
    }

    private fun isMinreact(it: KSAnnotation) = it.shortName.asString() == "ReactFunc"
}
