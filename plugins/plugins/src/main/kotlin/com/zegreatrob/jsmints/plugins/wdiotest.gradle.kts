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
    kotlin("multiplatform")
    base
}

repositories {
    mavenCentral()
}

kotlin {
    js {
        val e2eTest by compilations.creating
        binaries.executable(e2eTest)
    }
}

rootProject.extensions.findByType(NodeJsRootExtension::class.java).let {
    if (it?.version != "21.5.0") {
        it?.version = "21.5.0"
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
    attributes {
        attribute(Attribute.of("com.zegreatrob.executable", String::class.java), "runner")
    }
}

dependencies {
    "jsE2eTestImplementation"("com.zegreatrob.jsmints:wdio-testing-library:${PluginVersions.bomVersion}")
    "jsE2eTestImplementation"("com.zegreatrob.jsmints:wdiorunner:${PluginVersions.bomVersion}")
    runnerConfiguration("com.zegreatrob.jsmints:wdiorunner:${PluginVersions.bomVersion}") {
        artifact(fun DependencyArtifact.() {
            classifier = "executable"
        })
    }
}

afterEvaluate {
    dependencies {
        if (wdioTest.htmlReporter.get()) {
            "jsE2eTestImplementation"(npm("wdio-html-nice-reporter", PluginVersions.wdioNiceReporterVersion))
        }
        if (wdioTest.timelineReporter.get()) {
            "jsE2eTestImplementation"(npm("wdio-timeline-reporter", PluginVersions.wdioTimelineReporterVersion))
        }
        if (wdioTest.allureReporter.get()) {
            "jsE2eTestImplementation"(npm("@wdio/allure-reporter", PluginVersions.wdioAllureReporterVersion))
            "jsE2eTestImplementation"(npm("allure-commandline", PluginVersions.allureCLIVersion))
        }
    }
}

val npmProjectDir = kotlin.js().compilations.getByName("e2eTest").npmProject.dir

val wdioConfig = npmProjectDir.map { it.file("wdio.conf.mjs") }

tasks {
    val runnerJs = npmProjectDir.map { it.dir("runner").file("jsmints-wdiorunner.js") }
    val installRunner by registering(Copy::class) {
        dependsOn(runnerConfiguration)
        into(runnerJs.map { it.asFile.parentFile })
        from(
            zipTree(
                runnerConfiguration.resolve()
                    .first(),
            ),
        )
    }
    val wdioConfDirectory = npmProjectDir.map { it.file("wdio.conf.d") }

    val copyWdioConfDir by registering(Copy::class) {
        duplicatesStrategy = DuplicatesStrategy.WARN
        mustRunAfter(":rootPackageJson", ":kotlinNpmInstall")
        dependsOn("cleanCopyWdioConfDir")
        from(projectDir.resolve("wdio.conf.d")) {
            filter<ReplaceTokens>("tokens" to mapOf("HEADLESS" to wdioTest.useHeadless.get().toString()))
        }
        fun addPlugin(
            option: Property<Boolean>,
            pluginResource: URL,
            tokens: Map<String, Property<String?>> = mapOf(),
        ) {
            from(option.whenEnabledUseFile(pluginResource), fun CopySpec.() {
                this@from.rename { pluginResource.path.split("/").last<String>() }
                val stringTokens = tokens.mapValues { it.value.orNull ?: "" }
                    .plus(mapOf("HEADLESS" to wdioTest.useHeadless.get().toString()))
                filter<ReplaceTokens>("tokens" to stringTokens)
            })
        }
        addPlugin(wdioTest.useChrome, WdioTemplate.chromePlugin, mapOf("CHROME_BINARY" to wdioTest.chromeBinary))
        addPlugin(wdioTest.htmlReporter, WdioTemplate.htmlReporterPlugin)
        addPlugin(wdioTest.timelineReporter, WdioTemplate.timelineReporterPlugin)
        addPlugin(wdioTest.allureReporter, WdioTemplate.allureReporterPlugin)
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
        into(npmProjectDir)
        rename { "wdio.conf.mjs" }
    }

    named("compileE2eTestKotlinJs", fun Task.() {
        dependsOn(copyWdio)
    })

    val wdioTestModuleName = "wdio-dev-tests"
    val compileE2eTestDevelopmentExecutableKotlinJs =
        named<Kotlin2JsCompile>("compileE2eTestDevelopmentExecutableKotlinJs") {
            compilerOptions { moduleName.set(wdioTestModuleName) }
        }

    val e2eTestProcessResources = named<ProcessResources>("jsE2eTestProcessResources")

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

        val buildDir = project.layout.buildDirectory
        val reportDir = buildDir.dir("reports/e2e/")
        val testResultsDir = buildDir.dir("test-results/")
        outputs.dir(reportDir)
        outputs.dir(testResultsDir)
        outputs.cacheIf { true }

        val logsDir = buildDir.dir("reports/logs/e2e/")
        val specFile = kotlinJsCompilation.npmProject.dist.map { it.file("$wdioTestModuleName.js") }
        val nodeModules = rootProject.layout.buildDirectory.dir("js/node_modules")
        environment(
            mapOf(
                "BASEURL" to wdioTest.baseUrl.get(),
                "SPEC_FILE" to specFile.get().asFile.absolutePath,
                "WDIO_CONFIG" to wdioConfig.get().asFile.absolutePath,
                "REPORT_DIR" to reportDir.get().asFile.absolutePath,
                "TEST_RESULTS_DIR" to testResultsDir.get().asFile.absolutePath,

                "LOGS_DIR" to logsDir.get().asFile.absolutePath,
                "STRICT_SSL" to "false",
                "NODE_PATH" to listOf(
                    nodeModules.get().asFile.absolutePath,
                ).joinToString(":"),
            ),
        )
        npmProjectDir = kotlinJsCompilation.npmProject.dir.get().asFile
        arguments = listOf(runnerJs.get().asFile.absolutePath)
        val logFile = logsDir.get().file("run.log").asFile
        logFile.parentFile.mkdirs()
        outputFile = logFile

        verificationErrorMessage =
            listOfNotNull(
                "e2e tests failed.",
                if (wdioTest.htmlReporter.get()) {
                    "- report: file://${reportDir.get().asFile.absolutePath}/html/main-report.html"
                } else {
                    null
                },
                if (wdioTest.allureReporter.get()) {
                    "- report: file://${reportDir.get().asFile.absolutePath}/allure/report/index.html\n    This report must be viewed via a server in most browsers.\n    You can use IntelliJ to serve the file for viewing, or run your own server."
                } else {
                    null
                },
                if (wdioTest.allureReporter.get()) {
                    wdioTest.allureReportHint.get()
                        .ifEmpty { null }
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

fun Property<Boolean>.whenEnabledUseFile(pluginFile: URL): Provider<List<TextResource>> = zip(
    provider { resources.text.fromUri(pluginFile) },
) { shouldUse, htmlReporterFile ->
    if (shouldUse) {
        listOf(htmlReporterFile)
    } else {
        emptyList()
    }
}
