package com.zegreatrob.jsmints.plugins

import com.zegreatrob.jsmints.plugins.wdiotest.WdioTemplate
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
    base
}

repositories {
    mavenCentral()
}

kotlin {
    js {
        nodejs {
            testTask { enabled = false }
        }
        useCommonJs()
        binaries.executable()
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
    implementation("com.zegreatrob.jsmints:wdiorunner:${PluginVersions.bomVersion}")
    implementation("com.zegreatrob.jsmints:wdio-testing-library:${PluginVersions.bomVersion}")
}

afterEvaluate {
    dependencies {
        runnerConfiguration("com.zegreatrob.jsmints:wdiorunner:${PluginVersions.bomVersion}") {
            if (wdioTest.includedBuild) {
                targetConfiguration = "executable"
            } else {
                artifact { classifier = "executable" }
            }
        }
    }
}

val npmProjectDir = kotlin.js().compilations.getByName("test").npmProject.dir

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

    val productionExecutableCompileSync = named("productionExecutableCompileSync")
    val jsTestTestDevelopmentExecutableCompileSync = named("testTestDevelopmentExecutableCompileSync")
    val compileProductionExecutableKotlinJs =
        named("compileProductionExecutableKotlinJs", Kotlin2JsCompile::class) {}
    val compileE2eTestProductionExecutableKotlinJs =
        named("compileE2eTestProductionExecutableKotlinJs", Kotlin2JsCompile::class) {}

    val e2eTestProcessResources = named<ProcessResources>("e2eTestProcessResources")

    val e2eRun = register("e2eRun", NodeExec::class) {
        dependsOn(
            "copyWdio",
            installRunner,
            compileProductionExecutableKotlinJs,
            productionExecutableCompileSync,
            compileE2eTestProductionExecutableKotlinJs,
            jsTestTestDevelopmentExecutableCompileSync
        )
        setup(project)
        nodeModulesDir = e2eTestProcessResources.get().destinationDir
        moreNodeDirs = listOfNotNull(
            "${project.rootProject.buildDir.path}/js/node_modules",
            e2eTestProcessResources.get().destinationDir
        ).plus(project.relatedResources())
            .joinToString(":")

        inputs.files(compileProductionExecutableKotlinJs.map { it.outputs.files })
        inputs.files(compileE2eTestProductionExecutableKotlinJs.map { it.outputs.files })
        inputs.files(jsTestTestDevelopmentExecutableCompileSync.map { it.outputs.files })
        inputs.files(wdioConfig)

        val reportDir = "${project.buildDir.absolutePath}/reports/e2e/"
        val testResultsDir = "${project.buildDir.absolutePath}/test-results/"
        outputs.dir(reportDir)
        outputs.dir(testResultsDir)
        outputs.cacheIf { true }

        val logsDir = "${project.buildDir.absolutePath}/reports/logs/e2e/"
        environment(
            mapOf(
                "BASEURL" to "https://static.localhost",
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
    }

    afterEvaluate {
        register("copyWdio", Copy::class) {
            mustRunAfter(":rootPackageJson", ":kotlinNpmInstall")
            val wdioConfFile: File = wdioTest.wdioConfigFile ?: let {
                project.resources.text
                    .fromString(WdioTemplate.wdioTemplateText)
                    .asFile()
            }

            from(wdioConfFile) {
                filter<ReplaceTokens>(
                    "tokens" to mapOf(
                        "ENABLE_HTML_REPORTER" to "${wdioTest.htmlReporter}",
                        "USE_CHROME" to "${wdioTest.useChrome}"
                    )
                )
            }
            into(wdioConfig.parentFile)
            rename { "wdio.conf.mjs" }
        }
        dependencies {
            if (wdioTest.htmlReporter) {
                implementation(npm("wdio-html-nice-reporter", PluginVersions.wdioNiceReporterVersion))
            }
            if (wdioTest.useChrome) {
                implementation(npm("chromedriver", PluginVersions.chromedriverVersion))
                implementation(npm("wdio-chromedriver-service", PluginVersions.wdioChromedriverServiceVersion))
            }
        }
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
