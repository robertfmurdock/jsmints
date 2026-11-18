package com.zegreatrob.jsmints.plugins

import com.zegreatrob.jsmints.plugins.wdiotest.WdioTemplate
import com.zegreatrob.jsmints.plugins.wdiotest.WdioTestExtension
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBinaryMode
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import java.net.URL

plugins {
    kotlin("multiplatform")
    base
}

repositories {
    mavenCentral()
}

val jsTarget = kotlin.js()
val e2eCompilation = jsTarget.compilations.create("e2eTest")
val developmentBinary = jsTarget.binaries.executable(e2eCompilation)
    .single { it.mode == KotlinJsBinaryMode.DEVELOPMENT }

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

val wdioNpmProjectDir = e2eCompilation.npmProject.dir

val wdioConfig = wdioNpmProjectDir.map { it.file("wdio.conf.mjs") }

tasks {
    val runnerJs = wdioNpmProjectDir.map { it.dir("runner").file("jsmints-wdiorunner.js") }
    val installRunner = register<Copy>("installRunner") {
        dependsOn(runnerConfiguration)
        into(runnerJs.map { it.asFile.parentFile })
        from(provider { zipTree(runnerConfiguration.singleFile) })
    }
    val wdioConfDirectory = wdioNpmProjectDir.map { it.file("wdio.conf.d") }

    val copyWdioConfDir = register<Copy>("copyWdioConfDir") {
        duplicatesStrategy = DuplicatesStrategy.WARN
        dependsOn(":kotlinNpmInstall")
        dependsOn("cleanCopyWdioConfDir")
        val headless = wdioTest.useHeadless.map { it.toString() }
        inputs.property("headless", headless)
        from(projectDir.resolve("wdio.conf.d")) {
            eachFile {
                filter<ReplaceTokens>("tokens" to mapOf("HEADLESS" to headless.get()))
            }
        }
        fun addPlugin(
            option: Property<Boolean>,
            pluginResource: URL,
            tokens: Map<String, Property<String>> = mapOf(),
        ) {
            val tokenValues = tokens.mapValues { (_, value) -> value.orElse("") }
            tokenValues.forEach { (token, value) -> inputs.property(token, value) }
            from(option.whenEnabledUseFile(pluginResource)) {
                rename { pluginResource.path.substringAfterLast('/') }
                eachFile {
                    val replacements = tokenValues.mapValues { (_, value) -> value.get() }
                    filter<ReplaceTokens>("tokens" to (replacements + ("HEADLESS" to headless.get())))
                }
            }
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
        val baseUrl = wdioTest.baseUrl
        inputs.property("baseUrl", baseUrl)

        from(wdioConfFile) {
            eachFile {
                filter<ReplaceTokens>("tokens" to mapOf("BASE_URL" to baseUrl.get()))
            }
        }
        into(wdioNpmProjectDir)
        rename { "wdio.conf.mjs" }
    }

    e2eCompilation.compileTaskProvider.configure {
        dependsOn(copyWdio)
    }

    val wdioTestModuleName = "wdio-dev-tests"
    developmentBinary.linkTask.configure {
        compilerOptions { moduleName.set(wdioTestModuleName) }
        inputs.property("wdioTestModuleName", wdioTestModuleName)
    }
    val developmentSync = developmentBinary.linkSyncTask
    developmentSync.configure {
        inputs.property("wdioTestModuleName", wdioTestModuleName)
    }

    val e2eRun = register<WdioTest>("e2eRun") {
        group = "Verification"
        description = "This task will run WDIO end to end tests."
        setup(e2eCompilation)
        dependsOn(
            ":kotlinNpmInstall",
            copyWdio,
            installRunner,
        )

        inputs.files(developmentSync)
        inputs.files(wdioConfig)
        inputs.files(wdioConfDirectory)

        val buildDir = project.layout.buildDirectory
        val reportDir = buildDir.dir("reports/e2e")
        val testResultsDir = buildDir.dir("test-results/e2e")
        reportDirectory.set(reportDir)
        testResultsDirectory.set(testResultsDir)

        val logsDir = buildDir.dir("reports/logs/e2e")
        specCandidates.set(
            wdioNpmProjectDir.map { npmProjectDirectory ->
                val kotlinDirectory = npmProjectDirectory.dir("kotlin")
                listOf(
                    kotlinDirectory.file("$wdioTestModuleName.js"),
                    kotlinDirectory.file("${npmProjectDirectory.asFile.name}.js"),
                )
            },
        )
        val nodeModules = rootProject.layout.buildDirectory.dir("js/node_modules")
        defaultEnvironment("BASEURL", wdioTest.baseUrl)
        defaultEnvironment("WDIO_CONFIG", wdioConfig.map { it.asFile.absolutePath })
        defaultEnvironment("REPORT_DIR", reportDir.map { it.asFile.absolutePath })
        defaultEnvironment("TEST_RESULTS_DIR", testResultsDir.map { it.asFile.absolutePath })
        defaultEnvironment("LOGS_DIR", logsDir.map { it.asFile.absolutePath })
        defaultEnvironment("NODE_PATH", nodeModules.map { it.asFile.absolutePath })
        environment("STRICT_SSL", "false")
        npmWorkingDirectory.set(wdioNpmProjectDir)
        argumentList.set(runnerJs.map { listOf(it.asFile.absolutePath) })
        val logFile = logsDir.map { it.file("run.log") }
        logOutputFile.set(logFile)
        configureReportDiagnostics(reportDir, logFile, wdioTest.htmlReporter, wdioTest.allureReporter, wdioTest.allureReportHint)
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
