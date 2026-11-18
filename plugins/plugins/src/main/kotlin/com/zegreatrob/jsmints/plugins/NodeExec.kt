package com.zegreatrob.jsmints.plugins

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.VerificationException
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrCompilation
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import java.io.File
import java.io.FileOutputStream

@DisableCachingByDefault
abstract class NodeExec : AbstractExecTask<NodeExec>(NodeExec::class.java) {

    @Internal
    lateinit var projectNodeModulesDir: File

    @Internal
    lateinit var nodeBinDir: File

    @Internal
    lateinit var nodeExecPath: String

    @get:Internal
    abstract val npmWorkingDirectory: DirectoryProperty

    @get:Internal
    var npmProjectDir: File?
        get() = npmWorkingDirectory.asFile.orNull
        set(value) {
            npmWorkingDirectory.fileValue(value)
        }

    @get:OutputFile
    @get:Optional
    abstract val logOutputFile: RegularFileProperty

    @get:Internal
    var outputFile: File?
        get() = logOutputFile.asFile.orNull
        set(value) {
            logOutputFile.fileValue(value)
        }

    @Input
    @Optional
    var nodeCommand: String? = null

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    var nodeCommandDirectories: List<File> = emptyList()

    @get:Input
    @get:Optional
    abstract val verificationMessage: Property<String>

    @get:Internal
    var verificationErrorMessage: String?
        get() = verificationMessage.orNull
        set(value) = verificationMessage.set(value)

    @get:Input
    abstract val argumentList: ListProperty<String>

    @get:Internal
    var arguments: List<String>
        get() = argumentList.get()
        set(value) = argumentList.set(value)

    init {
        argumentList.convention(emptyList())
    }

    override fun exec() {
        npmWorkingDirectory.asFile.orNull?.let { workingDir = it }
        val commandFromBin = nodeCommand?.let { command ->
            val orderedDirectories = (nodeCommandDirectories + projectNodeModulesDir.resolve(".bin")).distinct()
            val commandPath = orderedDirectories
                .asSequence()
                .map { it.resolve(command) }
                .firstOrNull { it.exists() }
                ?: projectNodeModulesDir.resolve(".bin").resolve(command)
            listOf(commandPath.absolutePath)
        } ?: emptyList()
        commandLine = listOf(nodeExecPath) + commandFromBin + argumentList.get()

        logOutputFile.asFile.orNull?.let {
            it.parentFile?.mkdirs()
            standardOutput = FileOutputStream(it)
            errorOutput = standardOutput
        }

        kotlin.runCatching { super.exec() }
            .getOrElse { exception ->
                val message = verificationMessage.orNull
                if (message != null) {
                    throw VerificationException(message)
                } else {
                    throw exception
                }
            }
    }
}

fun NodeExec.setup(compilation: KotlinJsIrCompilation) {
    @Suppress("DEPRECATION")
    val nodeEnvironment = NodeJsRootPlugin.apply(project.rootProject).requireConfigured()
    nodeBinDir = nodeEnvironment.nodeBinDir
    nodeExecPath = nodeEnvironment.executable

    val npmProject = compilation.npmProject
    projectNodeModulesDir = npmProject.nodeModulesDir.get().asFile
    nodeCommandDirectories = listOf(
        npmProject.dir.get().asFile.resolve("node_modules/.bin"),
        projectNodeModulesDir.resolve(".bin"),
    )
}
