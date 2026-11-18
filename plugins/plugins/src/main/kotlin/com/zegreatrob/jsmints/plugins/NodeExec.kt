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
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import java.io.File
import java.io.FileOutputStream

@DisableCachingByDefault
abstract class NodeExec : AbstractExecTask<NodeExec>(NodeExec::class.java) {

    @get:Internal
    abstract val projectNodeModules: DirectoryProperty

    @get:Internal
    var projectNodeModulesDir: File
        get() = projectNodeModules.get().asFile
        set(value) {
            projectNodeModules.fileValue(value)
        }

    @Internal
    lateinit var nodeBinDir: File

    @get:Internal
    abstract val nodeExecutable: Property<String>

    @get:Internal
    var nodeExecPath: String
        get() = nodeExecutable.get()
        set(value) = nodeExecutable.set(value)

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

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val commandSearchDirectories: ListProperty<File>

    @get:Internal
    var nodeCommandDirectories: List<File>
        get() = commandSearchDirectories.get()
        set(value) = commandSearchDirectories.set(value)

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
        commandSearchDirectories.convention(emptyList())
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
    val nodeEnvironment = NodeJsPlugin.apply(project.rootProject)

    // NodeJsEnvSpec.env is internal in Kotlin 2.4.21. Keep this legacy field populated.
    @Suppress("DEPRECATION")
    val legacyEnvironment = NodeJsRootPlugin.apply(project.rootProject).requireConfigured()
    nodeBinDir = legacyEnvironment.nodeBinDir
    nodeExecutable.set(nodeEnvironment.executable)

    val npmProject = compilation.npmProject
    projectNodeModules.set(npmProject.nodeModulesDir)
    commandSearchDirectories.set(
        npmProject.dir.zip(npmProject.nodeModulesDir) { directory, modules ->
            listOf(directory.asFile.resolve("node_modules/.bin"), modules.asFile.resolve(".bin"))
        },
    )
}
