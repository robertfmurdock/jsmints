package com.zegreatrob.jsmints.plugins

import com.zegreatrob.jsmints.plugins.wdiotest.WdioTemplate
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.net.URL

plugins {
    kotlin("js")
    base
}

repositories {
    mavenCentral()
}

kotlin {
    js {
        compilations {
            val e2eTest by creating
            binaries.executable(e2eTest)
        }
    }
}

rootProject.extensions.findByType(NodeJsRootExtension::class.java).let {
    if (it?.nodeVersion != "19.6.0") {
        it?.nodeVersion = "19.6.0"
    }
}

rootProject.yarn.ignoreScripts = false

val wdioTest = project.extensions.create<WdioTestExtension>("wdioTest")

val runnerConfiguration: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    "e2eTestImplementation"("com.zegreatrob.jsmints:wdio-testing-library:${PluginVersions.bomVersion}")
    "e2eTestImplementation"("com.zegreatrob.jsmints:wdiorunner:${PluginVersions.bomVersion}")
    runnerConfiguration(
        wdioTest.includedBuild.map { isIncludedBuild ->
            create("com.zegreatrob.jsmints:wdiorunner:${PluginVersions.bomVersion}") {
                if (isIncludedBuild) {
                    targetConfiguration = "executable"
                } else {
                    artifact { classifier = "executable" }
                }
            }
        }
    )

    if (wdioTest.htmlReporter.get()) {
        "e2eTestImplementation"(npm("wdio-html-nice-reporter", PluginVersions.wdioNiceReporterVersion))
    }

    if (wdioTest.useChrome.get()) {
        "e2eTestImplementation"(npm("chromedriver", PluginVersions.chromedriverVersion))
        "e2eTestImplementation"(npm("wdio-chromedriver-service", PluginVersions.wdioChromedriverServiceVersion))
    }
}

val npmProjectDir = kotlin.js().compilations.getByName("e2eTest").npmProject.dir

val wdioConfig = npmProjectDir.resolve("wdio.conf.mjs")

tasks {
    val runnerJs = provider {
        npmProjectDir.resolve("runner")
            .resolve("wdio-test-plugin-wdiorunner.js")
    }
    val installRunner by registering(Copy::class) {
        dependsOn(runnerConfiguration)
        into(runnerJs.get().parentFile)
        from(
            zipTree(
                runnerConfiguration.resolve()
                    .first()
            )
        )
    }
    val wdioConfDirectory = wdioConfig.parentFile.resolve("wdio.conf.d")

    val copyWdioConfDir by registering(Copy::class) {
        mustRunAfter(":rootPackageJson", ":kotlinNpmInstall")
        dependsOn("cleanCopyWdioConfDir")
        from(projectDir.resolve("wdio.conf.d"))
        from(wdioTest.htmlReporter.whenEnabledUseFile(WdioTemplate.htmlReporterPluginText)) {
            rename { "html-reporter.mjs" }
        }
        into(wdioConfDirectory)
    }
    val copyWdio by registering(Copy::class) {
        mustRunAfter(":rootPackageJson", ":kotlinNpmInstall")
        dependsOn(copyWdioConfDir)
        val wdioConfFile = wdioTest.wdioConfigFile
            .map { it.asFile.toURI().toURL() }
            .orElse(WdioTemplate.wdioTemplateText)
            .map { resources.text.fromUri(it) }

        inputs.dir(wdioConfDirectory)

        from(wdioConfFile) {
            filter<ReplaceTokens>(
                "tokens" to mapOf(
                    "ENABLE_HTML_REPORTER" to "${wdioTest.htmlReporter.get()}",
                    "USE_CHROME" to "${wdioTest.useChrome.get()}"
                )
            )
        }
        into(wdioConfig.parentFile)
        rename { "wdio.conf.mjs" }
    }

    val compileE2eTestProductionExecutableKotlinJs =
        named("compileE2eTestProductionExecutableKotlinJs", Kotlin2JsCompile::class) {}

    val e2eTestProcessResources = named<ProcessResources>("e2eTestProcessResources")

    val e2eRun by registering(NodeExec::class) {
        group = "Verification"
        description = "This task will run WDIO end to end tests."

        dependsOn(
            copyWdio,
            installRunner,
            compileE2eTestProductionExecutableKotlinJs
        )
        setup(project)
        nodeModulesDir = e2eTestProcessResources.get().destinationDir
        moreNodeDirs = listOfNotNull(
            "${project.rootProject.buildDir.path}/js/node_modules",
            e2eTestProcessResources.get().destinationDir
        ).plus(project.relatedResources())
            .joinToString(":")

        inputs.files(compileE2eTestProductionExecutableKotlinJs.map { it.outputs.files })
        inputs.files(wdioConfig)
        inputs.files(wdioConfDirectory)

        val reportDir = "${project.buildDir.absolutePath}/reports/e2e/"
        val testResultsDir = "${project.buildDir.absolutePath}/test-results/"
        outputs.dir(reportDir)
        outputs.dir(testResultsDir)
        outputs.cacheIf { true }

        val logsDir = "${project.buildDir.absolutePath}/reports/logs/e2e/"
        environment(
            mapOf(
                "BASEURL" to wdioTest.baseUrl.get(),
                "SPEC_FILE" to compileE2eTestProductionExecutableKotlinJs.get().outputFileProperty.get(),
                "WDIO_CONFIG" to wdioConfig.absolutePath,
                "REPORT_DIR" to reportDir,
                "TEST_RESULTS_DIR" to testResultsDir,
                "LOGS_DIR" to logsDir,
                "STRICT_SSL" to "false",
                "NODE_PATH" to listOf(
                    "${project.rootProject.buildDir.path}/js/node_modules"
                ).joinToString(":")
            )
        )
        arguments = listOf(runnerJs.get().absolutePath)
        val logFile = file("$logsDir/run.log")
        logFile.parentFile.mkdirs()
        outputFile = logFile

        verificationErrorMessage =
            "e2e tests failed.\n- report: file://${reportDir}main-report.html\n- logs: file://${logFile.absolutePath}"
    }

    check {
        dependsOn(e2eRun)
    }
}

fun Project.relatedResources() = relatedProjects()
    .asSequence()
    .map { it.projectDir }
    .flatMap {
        listOf(
            "src/commonMain/resources",
            "src/clientCommonMain/resources",
            "src/jsMain/resources",
            "src/main/resources"
        ).asSequence().map(it::resolve)
    }
    .filter { it.exists() }
    .filter { it.isDirectory }
    .toList()

fun Project.relatedProjects(): Set<Project> {
    val configuration = configurations.findByName("e2eTestImplementation")
        ?: return emptySet()

    return configuration
        .allDependencies
        .asSequence()
        .filterIsInstance<DefaultProjectDependency>()
        .map { it.dependencyProject }
        .flatMap { sequenceOf(it) + it.relatedProjects() }
        .plus(this)
        .toSet()
}

fun Property<Boolean>.whenEnabledUseFile(pluginFile: URL) = zip(
    provider { resources.text.fromUri(pluginFile) }
) { shouldUse, htmlReporterFile ->
    if (shouldUse) {
        listOf(htmlReporterFile)
    } else {
        emptyList()
    }
}
