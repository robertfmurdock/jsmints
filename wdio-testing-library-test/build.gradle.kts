import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.io.FileOutputStream

plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.js")
    alias(libs.plugins.com.avast.gradle.docker.compose)
}

kotlin {
    js {
        useCommonJs()
        binaries.executable()
        nodejs { testTask { enabled = false } }
        compilations {
            val e2eTest by creating
            binaries.executable(e2eTest)
        }
    }
}

rootProject.yarn.ignoreScripts = false

dependencies {
    jsMainImplementation(kotlin("stdlib"))
    jsMainImplementation(project(":wdio-testing-library"))
    jsMainImplementation("com.soywiz.korlibs.klock:klock")
    jsMainImplementation("io.github.microutils:kotlin-logging")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    "jsE2eTestImplementation"(platform(project(":dependency-bom")))
    "jsE2eTestImplementation"(project(":wdio-testing-library"))
    "jsE2eTestImplementation"(kotlin("test"))
    "jsE2eTestImplementation"("com.zegreatrob.testmints:async")
    "jsE2eTestImplementation"("com.zegreatrob.testmints:minassert")
    "jsE2eTestImplementation"(jsconstraint("@rpii/wdio-html-reporter"))
    "jsE2eTestImplementation"(jsconstraint("@testing-library/webdriverio"))
    "jsE2eTestImplementation"(jsconstraint("@wdio/cli"))
    "jsE2eTestImplementation"(jsconstraint("@wdio/dot-reporter"))
    "jsE2eTestImplementation"(jsconstraint("@wdio/junit-reporter"))
    "jsE2eTestImplementation"(jsconstraint("@wdio/local-runner"))
    "jsE2eTestImplementation"(jsconstraint("@wdio/mocha-framework"))
    "jsE2eTestImplementation"(jsconstraint("chromedriver"))
    "jsE2eTestImplementation"(jsconstraint("wdio-chromedriver-service"))
}

dockerCompose {
    setProjectName("wdio-testing-library-test")
    containerLogToDir.set(project.file("build/test-output/containers-logs"))
    waitForTcpPorts.set(false)
}

tasks {
    named("jsNodeTest") {
        enabled = false
    }
    named("jsTest") {
        enabled = false
    }
    named("allTests") {
        enabled = false
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
            ":wdio-testing-library:jsTestTestDevelopmentExecutableCompileSync",
            "composeUp"
        )
        val wdioConfig = project.projectDir.resolve("wdio.conf.js")
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
                    "${project.rootProject.buildDir.path}/js/node_modules",
                ).joinToString(":"),
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
