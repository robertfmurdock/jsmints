package com.zegreatrob.jsmints.plugins

import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("multiplatform")
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

val runnerConfiguration: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    runnerConfiguration("com.zegreatrob.jsmints:wdiorunner") {
        targetConfiguration = "executable"
    }
    "jsMainImplementation"("com.zegreatrob.jsmints:wdiorunner")
    "jsMainImplementation"("com.zegreatrob.jsmints:wdio-testing-library")
}

tasks {
    register("runWdio", Exec::class) {
        dependsOn(runnerConfiguration)

        val executable = runnerConfiguration.resolve().first()
        commandLine = listOf("node", executable.absolutePath)
        outputs.cacheIf { true }
    }
    val productionExecutableCompileSync = named("jsProductionExecutableCompileSync")
    val jsTestTestDevelopmentExecutableCompileSync = named("jsTestTestDevelopmentExecutableCompileSync")
    val compileProductionExecutableKotlinJs =
        named("compileProductionExecutableKotlinJs", Kotlin2JsCompile::class) {}
    val compileE2eTestProductionExecutableKotlinJs =
        named("compileE2eTestProductionExecutableKotlinJs", Kotlin2JsCompile::class) {}

    val e2eTestProcessResources = named<ProcessResources>("jsE2eTestProcessResources")

    val e2eRun = register("e2eRun", NodeExec::class) {
        dependsOn(
            compileProductionExecutableKotlinJs,
            productionExecutableCompileSync,
            compileE2eTestProductionExecutableKotlinJs,
            jsTestTestDevelopmentExecutableCompileSync
        )
        setup(project)
        nodeModulesDir = e2eTestProcessResources.get().destinationDir
        moreNodeDirs = listOf(
            "${project.rootProject.buildDir.path}/js/node_modules",
            e2eTestProcessResources.get().destinationDir
        ).plus(project.relatedResources())
            .joinToString(":")

        val wdioConfig = project.projectDir.resolve("wdio.conf.mjs")
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

        environment("BASEURL" to "https://static.localhost")
        environment(
            mapOf(
                "SPEC_FILE" to compileE2eTestProductionExecutableKotlinJs.get().outputFileProperty.get(),
                "WDIO_CONFIG" to wdioConfig.absolutePath,
                "REPORT_DIR" to reportDir,
                "TEST_RESULTS_DIR" to testResultsDir,
                "STRICT_SSL" to "false",
                "NODE_PATH" to listOf(
                    "${project.rootProject.buildDir.path}/js/node_modules"
                ).joinToString(":")
            )
        )
        val logFile = file("$logsDir/run.log")
        logFile.parentFile.mkdirs()
        outputFile = logFile
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
