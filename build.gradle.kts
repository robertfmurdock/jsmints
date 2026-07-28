import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

plugins {
    alias(libs.plugins.com.github.sghill.distribution.sha)
    id("com.zegreatrob.jsmints.plugins.versioning")
    alias(libs.plugins.com.zegreatrob.tools.tagger)
    alias(libs.plugins.com.zegreatrob.tools.digger)
    alias(libs.plugins.com.zegreatrob.tools.fingerprint)
    base
}

group = "com.zegreatrob.jsmints"


tagger {
    releaseBranch = "master"
    githubReleaseEnabled.set(true)
    System.getenv("DISABLE_DETACHED")?.let { value ->
        allowDetachedHead = value.lowercase() != "true"
    }
}

fingerprintConfig {
    includedBuilds = listOf("libraries", "plugins")
}

abstract class VerifyCheckAggregation @Inject constructor(
    private val execOperations: ExecOperations,
) : DefaultTask() {

    @get:Internal
    abstract val rootDirectory: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val settingsFiles: ConfigurableFileCollection

    @TaskAction
    fun verify() {
        val rootDir = rootDirectory.get().asFile
        val includedBuildDirectories = Regex("""includeBuild\("([^"]+)"\)""")
            .findAll(rootDir.resolve("settings.gradle.kts").readText())
            .map { rootDir.resolve(it.groupValues[1]).canonicalFile }
            .toList()

        val includeRegex = Regex("""\binclude\(([^)]*)\)""")
        val quotedValueRegex = Regex(""""([^"]+)"""")

        val expectedCheckTasks = includedBuildDirectories
            .flatMap { includedBuildDirectory ->
                val includedBuildPath = includedBuildDirectory.name
                val includedBuildSettings = includedBuildDirectory.resolve("settings.gradle.kts")
                val includedProjects = if (includedBuildSettings.exists()) {
                    includeRegex.findAll(includedBuildSettings.readText())
                        .flatMap { quotedValueRegex.findAll(it.groupValues[1]) }
                        .map { it.groupValues[1].trimStart(':') }
                        .toList()
                } else {
                    emptyList()
                }

                if (includedProjects.isEmpty()) {
                    listOf(":$includedBuildPath:check")
                } else {
                    includedProjects.map { ":$includedBuildPath:$it:check" }
                }
            }
            .sorted()

        val output = ByteArrayOutputStream()
        val result = execOperations.exec {
            workingDir = rootDir
            commandLine(
                if (System.getProperty("os.name").lowercase().contains("windows")) {
                    "gradlew.bat"
                } else {
                    "./gradlew"
                },
                "check",
                "--dry-run",
                "--no-configuration-cache",
                "-PskipCheckAggregationVerification=true",
            )
            standardOutput = output
            errorOutput = output
            isIgnoreExitValue = true
        }
        val dryRunOutput = output.toString()
        if (result.exitValue != 0) {
            throw GradleException("Could not inspect root check task graph.\n$dryRunOutput")
        }

        val missingCheckTasks = expectedCheckTasks.filter { "$it " !in dryRunOutput && "$it\n" !in dryRunOutput }
        if (missingCheckTasks.isNotEmpty()) {
            throw GradleException(
                "Root check does not include these expected check tasks:\n" +
                    missingCheckTasks.joinToString(separator = "\n") +
                    "\n\nRun ./gradlew check --dry-run --no-configuration-cache to inspect the task graph."
            )
        }
    }
}

tasks {
    val verifyCheckAggregation = register<VerifyCheckAggregation>("verifyCheckAggregation") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Verifies root check reaches check tasks in included builds and their subprojects."
        rootDirectory.set(layout.projectDirectory)
        settingsFiles.from(layout.projectDirectory.file("settings.gradle.kts"))
        Regex("""includeBuild\("([^"]+)"\)""")
            .findAll(layout.projectDirectory.file("settings.gradle.kts").asFile.readText())
            .map { layout.projectDirectory.file("${it.groupValues[1]}/settings.gradle.kts") }
            .forEach { settingsFiles.from(it) }
    }

    assemble {
        dependsOn(aggregateFingerprints)
    }
    val publishableBuilds = listOf(
        gradle.includedBuild("libraries"),
        gradle.includedBuild("plugins"),
    )
    val testBuilds = listOf(
        gradle.includedBuild("libraries"),
        gradle.includedBuild("plugins"),
    ) + gradle.includedBuild("wdio-testing-library-test")
    val includedBuilds = testBuilds + gradle.includedBuild("convention-plugins")

    val publish = register("publish") {
        mustRunAfter(check)
        dependsOn(provider { publishableBuilds.map { it.task(":publish") } })
    }
    "versionCatalogUpdate" {
        dependsOn(provider { includedBuilds.map { it.task(":versionCatalogUpdate") } })
    }
    register("kotlinUpgradeYarnLock") {
        dependsOn(
            provider {
                listOf(
                    gradle.includedBuild("libraries"),
                    gradle.includedBuild("wdio-testing-library-test")
                ).map { it.task(":kotlinUpgradeYarnLock") }
            }
        )
    }
    register<Copy>("collectResults") {
        dependsOn(provider { (getTasksByName("collectResults", true) - this).toList() })
        dependsOn(provider { testBuilds.map { it.task(":collectResults") } })
        from(testBuilds.map { it.projectDir.resolve("build/test-output") })
        into(rootProject.layout.buildDirectory.dir("test-output/${project.path}".replace(":", "/")))
    }

    register("formatKotlin") {
        dependsOn(provider { (getTasksByName("formatKotlin", true) - this).toList() })
        dependsOn(provider { includedBuilds.map { it.task(":formatKotlin") } })
    }
    check {
        if (!providers.gradleProperty("skipCheckAggregationVerification").isPresent) {
            dependsOn(verifyCheckAggregation)
        }
        dependsOn(provider { (getTasksByName("check", true) - this).toList() })
        dependsOn(provider { includedBuilds.map { it.task(":check") } })

    }
    clean {
        dependsOn(provider { includedBuilds.map { it.task(":clean") } })
    }

    release {
        mustRunAfter(check)
        finalizedBy(publish, currentContributionData)
    }
    currentContributionData {
        mustRunAfter(tag)
    }
}
