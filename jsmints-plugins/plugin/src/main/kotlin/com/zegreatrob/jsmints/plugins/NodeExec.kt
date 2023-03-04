package com.zegreatrob.jsmints.plugins

import org.gradle.api.Project
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.VerificationException
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import java.io.File
import java.io.FileOutputStream

open class NodeExec : AbstractExecTask<NodeExec>(NodeExec::class.java) {

    @Internal
    lateinit var projectNodeModulesDir: File

    @Internal
    lateinit var nodeBinDir: File

    @Internal
    lateinit var nodeExecPath: String

    @Internal
    var nodeModulesDir: File? = null

    @Input
    @Optional
    var moreNodeDirs: String? = null

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
        // environment(
        //     "NODE_PATH",
        //     listOfNotNull(nodeModulesDir, projectNodeModulesDir, moreNodeDirs)
        //         .joinToString(":")
        // )
        // environment("PATH", "$nodeBinDir")
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

fun NodeExec.setup(compilation: KotlinJsCompilation) {
    val nodeJs = NodeJsRootPlugin.apply(project.rootProject)
    nodeBinDir = nodeJs.requireConfigured().nodeBinDir
    nodeExecPath = nodeJs.requireConfigured().nodeExecutable
    projectNodeModulesDir = compilation.npmProject.nodeModulesDir
}

val Project.nodeModulesDir get() = rootProject.buildDir.resolve("js/node_modules")
