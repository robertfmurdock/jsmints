import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
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
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import com.squareup.kotlinpoet.ksp.writeTo

class MinreactProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
    var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }
        invoked = true

        val visitor = MinreactVisitor(logger)
        resolver.getAllFiles().forEach {
            it.accept(visitor, codeGenerator)
        }
        return emptyList()
    }
}

class MinreactVisitor(private val logger: KSPLogger) : KSTopDownVisitor<CodeGenerator, Unit>() {
    override fun defaultHandler(node: KSNode, data: CodeGenerator) {
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: CodeGenerator) {
        super.visitClassDeclaration(classDeclaration, data)
        if (classDeclaration.getAllSuperTypes().map { it.toClassName() }.contains(ClassName("react", "Props"))) {
            val resolver = classDeclaration.typeParameters.toTypeParameterResolver()

            val builder = FileSpec.builder(classDeclaration.packageName.asString(), "${classDeclaration}Extentions")
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

            var paramsAssignments = propertiesAsParameterAssignments(classDeclaration)

            val childrenNode = classDeclaration.getAllProperties().find(::isChildrenNode)
            var bodyArgs = listOf<Any?>(MemberName("react", "create"))
            if (childrenNode != null) {
                paramsAssignments += "\n%T { children() }\n"
                bodyArgs = bodyArgs.plusElement(
                    ClassName("react", "Fragment")
                )
            }
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
                var paramsAssignments = propertiesAsParameterAssignments(classDeclaration)

                val childrenNode = classDeclaration.getAllProperties().find(::isChildrenNode)
                var bodyArgs = listOf<Any?>(parameterizedTypeName(classDeclaration))
                if (childrenNode != null) {
                    paramsAssignments += "\n%T { children() }\n"
                    bodyArgs = bodyArgs + listOf(
                        ClassName("react", "Fragment")
                    )
                }

                val body = """
                    |  val component = ($targetName.unsafeCast<%T>())
                    |  component {
                    |       $paramsAssignments
                    |       }
                """.trimMargin()



                FileSpec.builder(property.packageName.asString(), "${targetName}Kt")
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

    private fun propertiesAsParameterAssignments(classDeclaration: KSClassDeclaration) =
        classDeclaration.getAllProperties()
            .filterNot(::isChildrenNode)
            .joinToString("\n", transform = ::assignPropByParameter)

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

    private fun assignPropByParameter(property: KSPropertyDeclaration) = if (property.type.resolve().isMarkedNullable) {
        "${property.simpleName.getShortName()}?.let { this.${property.simpleName.getShortName()} = it }"
    } else {
        "this.${property.simpleName.getShortName()} = ${property.simpleName.getShortName()}"
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
                    it + childrenParameter()
                }
            }
            .asIterable()
    }

    private fun childrenParameter() = ParameterSpec.builder(
        "children", LambdaTypeName.get(
            receiver = ClassName("react", "ChildrenBuilder"),
            parameters = emptyList(),
            returnType = UNIT
        )
    )
        .defaultValue("{}")
        .build()

    private fun isChildrenNode(it: KSPropertyDeclaration) = it.simpleName.asString() == "children"

    private fun parameterSpec(
        it: KSPropertyDeclaration,
        resolver: TypeParameterResolver
    ): ParameterSpec {
        val typeName = it.type.toTypeName(resolver)
        val builder = ParameterSpec.builder(it.simpleName.getShortName(), typeName)

        if (typeName.isNullable) {
            builder.defaultValue(CodeBlock.of("null"))
        }

        return builder
            .build()
    }

    private fun isMinreact(it: KSAnnotation) = it.shortName.asString() == "ReactFunc"
}

class TestProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) =
        MinreactProcessor(environment.codeGenerator, environment.logger)
}