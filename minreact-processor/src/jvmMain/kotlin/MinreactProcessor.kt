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
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
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

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: CodeGenerator) {
        super.visitPropertyDeclaration(property, data)
        val targetName = property.simpleName.getShortName()

        if (property.annotations.any(::isMinreact)) {
            val resolvedType: KSType = property.type.resolve()

            resolvedType.arguments.forEach {
                val propsType = it.type?.resolve()
                val classDeclaration = propsType?.declaration as? KSClassDeclaration
                    ?: return@forEach
                var paramsAssignments = classDeclaration.getAllProperties()
                    .filterNot(::isChildrenNode)
                    .joinToString("\n", transform = ::assignPropByParameter)

                val childrenNode = classDeclaration.getAllProperties().find(::isChildrenNode)
                if (childrenNode != null) {
                    paramsAssignments += "\n+%T.%M{ children() }\n"
                }

                val body = """$targetName {
                    |       $paramsAssignments
                    |       }
                """.trimMargin()

                val resolver = classDeclaration.typeParameters.toTypeParameterResolver()

                FileSpec.builder(property.packageName.asString(), "${targetName}Kt")
                    .addFunction(
                        FunSpec.builder(targetName)
                            .addParameters(parameterSpecs(classDeclaration, resolver))
                            .receiver(ClassName("react", "ChildrenBuilder"))
                            .returns(Unit::class)
                            .addCode(CodeBlock.of(body, ClassName("react", "Fragment"), MemberName("react", "create")))
                            .build()
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

    private fun assignPropByParameter(property: KSPropertyDeclaration) =
        "this.${property.simpleName.getShortName()} = ${property.simpleName.getShortName()}"

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
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return MinreactProcessor(environment.codeGenerator, environment.logger)
    }
}