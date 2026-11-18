package com.zegreatrob.jsmints.plugins

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.options.Option
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Consumers configure caching through the task outputs API")
abstract class WdioTest : NodeExec() {

    @Option(option = "tests", description = "Allows test matcher to be specified")
    @Input
    @Optional
    var tests: String? = null

    // The link sync task already supplies these files as inputs.
    @get:Internal
    abstract val specCandidates: ListProperty<RegularFile>

    @get:OutputDirectory
    @get:Optional
    abstract val reportDirectory: DirectoryProperty

    @get:OutputDirectory
    @get:Optional
    abstract val testResultsDirectory: DirectoryProperty

    @get:Internal
    abstract val environmentDefaults: MapProperty<String, String>

    @get:Input
    val wdioEnvironment: Map<String, String>
        get() = environmentDefaults.get().mapNotNull { (name, defaultValue) ->
            when (val current = environment[name]) {
                EnvironmentDefault.VALUE -> name to defaultValue
                null -> null
                else -> name to current.toString()
            }
        }.toMap()

    fun defaultEnvironment(name: String, value: Provider<String>) {
        environmentDefaults.put(name, value)
        environment(name, EnvironmentDefault.VALUE)
    }

    internal fun configureReportDiagnostics(
        reportDir: Provider<Directory>,
        logFile: Provider<RegularFile>,
        htmlReporter: Provider<Boolean>,
        allureReporter: Provider<Boolean>,
        allureReportHint: Provider<String>,
    ) {
        val message = reportDir.zip(logFile) { report, log ->
            val reportPath = report.asFile.absolutePath
            val includeAllure = allureReporter.get()
            listOfNotNull(
                "e2e tests failed.",
                if (htmlReporter.get()) "- report: file://$reportPath/html/main-report.html" else null,
                if (includeAllure) {
                    "- report: file://$reportPath/allure/report/index.html\n    This report must be viewed via a server in most browsers. You can use IntelliJ to serve the file for viewing, or run your own server."
                } else {
                    null
                },
                if (includeAllure) allureReportHint.get().ifEmpty { null } else null,
                "- logs: file://${log.asFile.absolutePath}",
            ).joinToString("\n")
        }
        verificationMessage.set(message)
    }

    override fun exec() {
        environment(wdioEnvironment)
        val candidates = specCandidates.get()
        if (candidates.isNotEmpty()) {
            val specFile = candidates.firstOrNull { it.asFile.isFile }
                ?: error("WDIO spec output was not created: ${candidates.joinToString()}")
            environment("SPEC_FILE", specFile.asFile.absolutePath)
        }
        testResultsDirectory.asFile.orNull?.deleteRecursively()
        if (tests != null) {
            environment(mapOf("FGREP" to tests))
        }
        super.exec()
    }

    // Reserve a key without resolving its provider so later ExecSpec overrides still win.
    private enum class EnvironmentDefault {
        VALUE,
    }
}
