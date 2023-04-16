package com.zegreatrob.jsmints.plugins

import com.zegreatrob.jsmints.plugins.wdiotest.WdioTemplate
import com.zegreatrob.jsmints.plugins.wdiotest.WdioTestExtension
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
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

val yarnAutoReplace: String? by project

rootProject.yarn.yarnLockAutoReplace = yarnAutoReplace != null
rootProject.yarn.yarnLockMismatchReport = if (yarnAutoReplace != null) {
    YarnLockMismatchReport.WARNING
} else {
    YarnLockMismatchReport.FAIL
}

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
        },
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
            .resolve("jsmints-plugins-wdiorunner.js")
    }
    val installRunner by registering(Copy::class) {
        dependsOn(runnerConfiguration)
        into(runnerJs.get().parentFile)
        from(
            zipTree(
                runnerConfiguration.resolve()
                    .first(),
            ),
        )
    }
    val wdioConfDirectory = wdioConfig.parentFile.resolve("wdio.conf.d")

    val copyWdioConfDir by registering(Copy::class) {
        mustRunAfter(":rootPackageJson", ":kotlinNpmInstall")
        dependsOn("cleanCopyWdioConfDir")
        from(projectDir.resolve("wdio.conf.d"))
        fun addPlugin(option: Property<Boolean>, pluginResource: URL) {
            from(option.whenEnabledUseFile(pluginResource)) {
                rename { pluginResource.path.split("/").last() }
            }
        }
        addPlugin(wdioTest.useChrome, WdioTemplate.chromePlugin)
        addPlugin(wdioTest.htmlReporter, WdioTemplate.htmlReporterPlugin)
        addPlugin(wdioTest.screenshotsOnFailure, WdioTemplate.screenshotsOnFailurePlugin)
        into(wdioConfDirectory)
    }
    val copyWdio by registering(Copy::class) {
        mustRunAfter(":rootPackageJson", ":kotlinNpmInstall")
        dependsOn(copyWdioConfDir)
        val wdioConfFile = wdioTest.wdioConfigFile
            .map { it.asFile.toURI().toURL() }
            .orElse(WdioTemplate.wdioTemplate)
            .map { resources.text.fromUri(it) }

        inputs.dir(wdioConfDirectory)

        from(wdioConfFile) {
            filter<ReplaceTokens>(
                "tokens" to mapOf(
                    "BASE_URL" to wdioTest.baseUrl.get(),
                ),
            )
        }
        into(wdioConfig.parentFile)
        rename { "wdio.conf.mjs" }
    }

    named("compileE2eTestKotlinJs") {
        dependsOn(copyWdio)
    }

    val wdioTestModuleName = "wdio-dev-tests"
    val compileE2eTestDevelopmentExecutableKotlinJs =
        named("compileE2eTestDevelopmentExecutableKotlinJs", Kotlin2JsCompile::class) {
            compilerOptions { moduleName.set(wdioTestModuleName) }
        }

    val e2eTestProcessResources = named<ProcessResources>("e2eTestProcessResources")

    val e2eRun by registering(WdioTest::class) {
        group = "Verification"
        description = "This task will run WDIO end to end tests."
        val kotlinJsCompilation = kotlin.js().compilations["e2eTest"]
        setup(kotlinJsCompilation)
        dependsOn(
            copyWdio,
            installRunner,
            e2eTestProcessResources,
            compileE2eTestDevelopmentExecutableKotlinJs,
        )

        inputs.files(compileE2eTestDevelopmentExecutableKotlinJs.map { it.outputs.files })
        inputs.files(wdioConfig)
        inputs.files(wdioConfDirectory)

        val reportDir = "${project.buildDir.absolutePath}/reports/e2e/"
        val testResultsDir = "${project.buildDir.absolutePath}/test-results/"
        outputs.dir(reportDir)
        outputs.dir(testResultsDir)
        outputs.cacheIf { true }

        val logsDir = "${project.buildDir.absolutePath}/reports/logs/e2e/"
        val specFile = kotlinJsCompilation.npmProject.dist.resolve("$wdioTestModuleName.js")
        environment(
            mapOf(
                "BASEURL" to wdioTest.baseUrl.get(),
                "SPEC_FILE" to specFile,
                "WDIO_CONFIG" to wdioConfig.absolutePath,
                "REPORT_DIR" to reportDir,
                "TEST_RESULTS_DIR" to testResultsDir,

                "LOGS_DIR" to logsDir,
                "STRICT_SSL" to "false",
                "NODE_PATH" to listOf(
                    "${project.rootProject.buildDir.path}/js/node_modules",
                ).joinToString(":"),
            ),
        )
        arguments = listOf(runnerJs.get().absolutePath)
        val logFile = file("$logsDir/run.log")
        logFile.parentFile.mkdirs()
        outputFile = logFile

        verificationErrorMessage =
            listOfNotNull(
                "e2e tests failed.",
                if (wdioTest.htmlReporter.get()) {
                    "- report: file://${reportDir}html/main-report.html"
                } else {
                    null
                },
                "- logs: file://${logFile.absolutePath}",
            ).joinToString("\n")
    }

    check {
        dependsOn(e2eRun)
    }
}

fun Property<Boolean>.whenEnabledUseFile(pluginFile: URL) = zip(
    provider { resources.text.fromUri(pluginFile) },
) { shouldUse, htmlReporterFile ->
    if (shouldUse) {
        listOf(htmlReporterFile)
    } else {
        emptyList()
    }
}
