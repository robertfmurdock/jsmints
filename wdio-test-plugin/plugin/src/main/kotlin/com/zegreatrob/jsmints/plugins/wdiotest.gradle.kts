package com.zegreatrob.jsmints.plugins

import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.io.FileOutputStream

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

    val nodeRun = named("jsNodeRun", NodeJsExec::class) {
        dependsOn(
            compileProductionExecutableKotlinJs,
            productionExecutableCompileSync,
            compileE2eTestProductionExecutableKotlinJs,
            jsTestTestDevelopmentExecutableCompileSync,
            "composeUp"
        )
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
        standardOutput = FileOutputStream(logFile, true)
        errorOutput = standardOutput
    }
    check {
        dependsOn(nodeRun)
    }
}
