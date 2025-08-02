package com.zegreatrob.jsmints.plugins

import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.VerificationException
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrCompilation
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import java.io.File
import java.io.FileOutputStream

abstract class NodeExec : AbstractExecTask<NodeExec>(NodeExec::class.java) {

    @Internal
    lateinit var projectNodeModulesDir: File

    @Internal
    lateinit var nodeBinDir: File

    @Internal
    lateinit var nodeExecPath: String

    @Internal
    var npmProjectDir: File? = null

    @OutputFile
    @Optional
    var outputFile: File? = null

    @Input
    @Optional
    var nodeCommand: String? = null

    @Input
    @Optional
    var verificationErrorMessage: String? = null

    @Input
    var arguments: List<String> = emptyList()

    override fun exec() {
        npmProjectDir?.let { workingDir = it }
        val commandFromBin = nodeCommand?.let { listOf("$projectNodeModulesDir/.bin/$nodeCommand") } ?: emptyList()
        commandLine = listOf(nodeExecPath) + commandFromBin + arguments

        outputFile?.let {
            standardOutput = FileOutputStream(it)
            errorOutput = standardOutput
        }

        kotlin.runCatching { super.exec() }
            .getOrElse { exception ->
                val message = verificationErrorMessage
                if (message != null) {
                    throw VerificationException(message)
                } else {
                    throw exception
                }
            }
    }
}

fun NodeExec.setup(compilation: KotlinJsIrCompilation) {
    val nodeJs = NodeJsRootPlugin.apply(project.rootProject)
    @Suppress("DEPRECATION")
    nodeBinDir = nodeJs.requireConfigured().nodeBinDir
    @Suppress("DEPRECATION")
    nodeExecPath = nodeJs.requireConfigured().executable
    projectNodeModulesDir = compilation.npmProject.nodeModulesDir.get().asFile
}
