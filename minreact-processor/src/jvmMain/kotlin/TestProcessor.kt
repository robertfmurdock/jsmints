import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import java.io.OutputStreamWriter

class TestProcessor(val codeGenerator: CodeGenerator, val logger: KSPLogger) : SymbolProcessor {
    var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val allFiles = resolver.getAllFiles().map { it.fileName }
        logger.warn(allFiles.toList().toString())
        if (invoked) {
            return emptyList()
        }
        invoked = true

        codeGenerator.createNewFile(Dependencies.ALL_FILES, "", "Foo", "kt").use { output ->
            OutputStreamWriter(output).use { writer ->
                writer.write("package com.example\n\n")
                writer.write("class Foo {\n")

                val visitor = ClassVisitor()
                resolver.getAllFiles().forEach {
                    it.accept(visitor, writer)
                }

                writer.write("}\n")
            }
        }

        codeGenerator.createNewFile(Dependencies.ALL_FILES, "", "Components", "kt").use { output ->
            OutputStreamWriter(output).use { writer ->
                writer.write("package com.example\n\n")

                val visitor = MinreactVisitor(logger)
                resolver.getAllFiles().forEach {
                    it.accept(visitor, writer)
                }
            }
        }
        return emptyList()
    }
}

class MinreactVisitor(val logger: KSPLogger) : KSTopDownVisitor<OutputStreamWriter, Unit>() {
    override fun defaultHandler(node: KSNode, data: OutputStreamWriter) {
    }

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: OutputStreamWriter
    ) {
        super.visitClassDeclaration(classDeclaration, data)

        val parentClassDeclaration = classDeclaration.parentDeclaration as? KSClassDeclaration
        if(parentClassDeclaration?.annotations?.any { it.shortName.asString() == "Minreact" } == true) {
            val targetName = parentClassDeclaration.simpleName.getShortName()

            logger.warn("class: $targetName ")

            val params = classDeclaration.primaryConstructor?.parameters?.map { "${it.name?.getShortName()}: ${it.type}" }
                ?.joinToString(", ")
            logger.warn("class: $params ")

            data.write("fun react.ChildrenBuilder.$targetName( $params ) {  }\n")
        }
    }

}

class ClassVisitor : KSTopDownVisitor<OutputStreamWriter, Unit>() {
    override fun defaultHandler(node: KSNode, data: OutputStreamWriter) {
    }

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: OutputStreamWriter
    ) {
        super.visitClassDeclaration(classDeclaration, data)
        val symbolName = classDeclaration.simpleName.asString().lowercase()
        classDeclaration.parentDeclaration
        val qualifiedName = classDeclaration.qualifiedName?.asString()?.lowercase()
        val parentName = classDeclaration.parentDeclaration?.qualifiedName?.asString()?.lowercase()
        data.write(
            """    val $symbolName = "$qualifiedName" // parent is $parentName
"""
        )
    }
}

class TestProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return TestProcessor(environment.codeGenerator, environment.logger)
    }
}