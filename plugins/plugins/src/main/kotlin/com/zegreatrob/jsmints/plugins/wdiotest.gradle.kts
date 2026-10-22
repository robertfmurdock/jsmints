package com.zegreatrob.jsmints.plugins

import com.zegreatrob.jsmints.plugins.wdiotest.WdioTemplate
import com.zegreatrob.jsmints.plugins.wdiotest.WdioTestExtension
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
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
        val e2eTest = compilations.create("e2eTest")
        binaries.executable(e2eTest)
    }
}

rootProject.yarn.ignoreScriptsProperty = false
val yarnAutoReplace = project.findProperty("yarnAutoReplace") as String?

rootProject.yarn.yarnLockAutoReplaceProperty = yarnAutoReplace != null
rootProject.yarn.yarnLockMismatchReportProperty = if (yarnAutoReplace != null) {
    YarnLockMismatchReport.WARNING
} else {
    YarnLockMismatchReport.FAIL
}

val wdioTest = project.extensions.create<WdioTestExtension>("wdioTest")

val runnerConfiguration = configurations.create("runnerConfiguration") {
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
        isTransitive = false
        artifact(fun DependencyArtifact.() {
            classifier = "executable"
        })
    }
}

dependencies {
    val e2eDependencies = configurations.named("jsE2eTestImplementation").get().dependencies
    e2eDependencies.addAllLater(
        wdioTest.htmlReporter.map { enabled ->
            if (enabled) listOf(npm("wdio-html-nice-reporter", PluginVersions.wdioNiceReporterVersion)) else emptyList()
        },
    )
    e2eDependencies.addAllLater(
        wdioTest.timelineReporter.map { enabled ->
            if (enabled) listOf(npm("wdio-timeline-reporter", PluginVersions.wdioTimelineReporterVersion)) else emptyList()
        },
    )
    e2eDependencies.addAllLater(
        wdioTest.allureReporter.map { enabled ->
            if (enabled) {
                listOf(
                    npm("@wdio/allure-reporter", PluginVersions.wdioAllureReporterVersion),
                    npm("allure-commandline", PluginVersions.allureCLIVersion),
                )
            } else {
                emptyList()
            }
        },
    )
}

val npmProjectDir = kotlin.js().compilations.getByName("e2eTest").npmProject.dir

val wdioConfig = npmProjectDir.map { it.file("wdio.conf.mjs") }

tasks {
    val runnerJs = npmProjectDir.map { it.dir("runner").file("jsmints-wdiorunner.js") }
    val installRunner = register<Copy>("installRunner") {
        dependsOn(runnerConfiguration)
        into(runnerJs.map { it.asFile.parentFile })
        from(provider { zipTree(runnerConfiguration.singleFile) })
    }
    val wdioConfDirectory = npmProjectDir.map { it.file("wdio.conf.d") }

    val copyWdioConfDir = register<Copy>("copyWdioConfDir") {
        duplicatesStrategy = DuplicatesStrategy.WARN
        dependsOn(":kotlinNpmInstall")
        dependsOn("cleanCopyWdioConfDir")
        from(projectDir.resolve("wdio.conf.d")) {
            filter<ReplaceTokens>("tokens" to mapOf("HEADLESS" to wdioTest.useHeadless.get().toString()))
        }
        fun addPlugin(
            option: Property<Boolean>,
            pluginResource: URL,
            tokens: Map<String, Property<String>> = mapOf(),
        ) {
            from(option.whenEnabledUseFile(pluginResource), fun CopySpec.() {
                rename { pluginResource.path.split("/").last<String>() }
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
    val copyWdio = register<Copy>("copyWdio") {
        dependsOn(":kotlinNpmInstall")
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
            inputs.property("wdioTestModuleName", wdioTestModuleName)
        }

    val e2eTestProcessResources = named<ProcessResources>("jsE2eTestProcessResources")
    val e2eTestDevelopmentExecutableCompileSync =
        named("jsE2eTestE2eTestDevelopmentExecutableCompileSync") {
            inputs.property("wdioTestModuleName", wdioTestModuleName)
        }

    val e2eRun = register<WdioTest>("e2eRun") {
        group = "Verification"
        description = "This task will run WDIO end to end tests."
        val kotlinJsCompilation = kotlin.js().compilations["e2eTest"]
        setup(kotlinJsCompilation)
        dependsOn(
            ":kotlinNpmInstall",
            copyWdio,
            installRunner,
            e2eTestProcessResources,
            compileE2eTestDevelopmentExecutableKotlinJs,
            e2eTestDevelopmentExecutableCompileSync,
        )

        inputs.files(e2eTestDevelopmentExecutableCompileSync)
        inputs.files(wdioConfig)
        inputs.files(wdioConfDirectory)

        val buildDir = project.layout.buildDirectory
        val reportDir = buildDir.dir("reports/e2e/")
        val testResultsDir = buildDir.dir("test-results/e2e")
        outputs.dir(reportDir)
        outputs.dir(testResultsDir)
        outputs.cacheIf { true }

        val logsDir = buildDir.dir("reports/logs/e2e/")
        val specFiles = kotlinJsCompilation.npmProject.dir.map { npmProjectDirectory ->
            val kotlinDirectory = npmProjectDirectory.dir("kotlin")
            listOf(
                kotlinDirectory.file("$wdioTestModuleName.js"),
                kotlinDirectory.file("${npmProjectDirectory.asFile.name}.js"),
            )
        }
        val nodeModules = rootProject.layout.buildDirectory.dir("js/node_modules")
        environment(
            mapOf(
                "BASEURL" to wdioTest.baseUrl.get(),
                "SPEC_FILE" to specFiles.get().first().asFile.absolutePath,
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
        doFirst {
            val specFile = specFiles.get().firstOrNull { it.asFile.isFile }
                ?: error("WDIO spec output was not created: ${specFiles.get().joinToString()}")
            environment("SPEC_FILE", specFile.asFile.absolutePath)
        }
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
