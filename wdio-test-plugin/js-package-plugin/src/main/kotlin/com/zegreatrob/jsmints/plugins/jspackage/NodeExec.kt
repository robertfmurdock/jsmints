package com.zegreatrob.jsmints.plugins.jspackage

import org.gradle.api.Project
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
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
    var arguments: List<String> = emptyList()

    override fun exec() {
        environment(
            "NODE_PATH",
            listOfNotNull(nodeModulesDir, projectNodeModulesDir, moreNodeDirs)
                .joinToString(":")
        )
        environment("PATH", "$nodeBinDir")
        npmProjectDir?.let { workingDir = it }
        val commandFromBin = nodeCommand?.let { listOf("$projectNodeModulesDir/.bin/$nodeCommand") } ?: emptyList()
        commandLine = listOf(nodeExecPath) + commandFromBin + arguments

        outputFile?.let {
            standardOutput = FileOutputStream(it)
            errorOutput = standardOutput
        }

        super.exec()
    }
}

fun NodeExec.setup(project: Project) {
    projectNodeModulesDir = project.nodeModulesDir
    nodeBinDir = project.nodeBinDir
    nodeExecPath = project.nodeExecPath
}

private fun Project.goGetNodeBinDir(): File {
    val props = System.getProperties()
    fun property(name: String) = props.getProperty(name) ?: System.getProperty(name)
    val win = "win"
    val darwin = "darwin"
    val linux = "linux"
    val sunos = "sunos"

    val platform: String = run {
        val name = property("os.name").lowercase()
        when {
            name.contains("windows") -> win
            name.contains("mac") -> darwin
            name.contains("linux") -> linux
            name.contains("freebsd") -> linux
            name.contains("sunos") -> sunos
            else -> throw IllegalArgumentException("Unsupported OS: $name")
        }
    }
    val x64 = "x64"
    val x86 = "x86"
    val arm64 = "arm64"

    val architecture: String = run {
        val arch = property("os.arch").lowercase()
        when {
            arch.contains("64") -> x64
            arch == "arm" -> {
                // as Java just returns "arm" on all ARM variants, we need a system call to determine the exact arch
                // the node binaries for 'armv8l' are called 'arm64', so we need to distinguish here
                val process = Runtime.getRuntime().exec("uname -m")
                val systemArch = process.inputStream.bufferedReader().use { it.readText() }
                when (systemArch.trim()) {
                    "armv8l" -> arm64
                    else -> systemArch
                }
            }
            else -> x86
        }
    }
    val nodeJs = NodeJsRootPlugin.apply(this)
    val installationDir = nodeJs.installationDir
    val nodeDir = installationDir.resolve("node-v${nodeJs.nodeVersion}-$platform-$architecture")

    val isWindows = platform == win
    return if (isWindows) nodeDir else nodeDir.resolve("bin")
}

val Project.nodeModulesDir get() = rootProject.buildDir.resolve("js/node_modules")
private val Project.nodeExecPath get() = "$nodeBinDir/node"
private val Project.nodeBinDir get() = project.rootProject.goGetNodeBinDir()