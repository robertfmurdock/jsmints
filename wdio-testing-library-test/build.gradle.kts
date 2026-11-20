import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec

plugins {
    alias(rootLibs.plugins.com.zegreatrob.tools.dashboard.test.summary)
    id("com.zegreatrob.jsmints.plugins.js2")
    id("com.zegreatrob.jsmints.plugins.wdiotest")
    id("com.zegreatrob.jsmints.plugins.playwrighttest")
    id("com.zegreatrob.jsmints.plugins.versioning")
    alias(libs.plugins.com.avast.gradle.docker.compose)
}

kotlin {
    js {
        nodejs {
            testTask {
                enabled = false
            }
        }
    }
}

extensions.configure<NodeJsEnvSpec> {
    version.set(providers.fileContents(layout.projectDirectory.file("../.nvmrc")).asText.map { it.trim() })
    if (providers.environmentVariable("CI").isPresent) {
        download.set(false)
    }
}

val wdioHttpsPort = providers.environmentVariable("JSMINTS_WDIO_HTTPS_PORT")
    .filter { it.isNotEmpty() }
    .map { it.toInt() }
    .orElse(443)

val wdioBaseUrl = wdioHttpsPort.map { port ->
    if (port == 443) "https://static.localhost" else "https://static.localhost:$port"
}

playwrightTest {
    profiles {
        register("chromium")
        register("firefox")
    }
    baseUrl.set(wdioBaseUrl)
}

wdioTest {
    useChrome.set(true)
    chromeBinary.set(System.getenv("WDIO_CHROME_BINARY"))
    useHeadless.set(true)
    htmlReporter.set(true)
    allureReporter.set(true)
    allureReportHint.set("- link: http://localhost:63342/jsmints-root/wdio-testing-library-test/build/reports/e2e/allure/report/index.html")
    baseUrl.set(wdioBaseUrl)
}

dependencies {
    jsMainImplementation(platform("com.zegreatrob.jsmints:dependency-bom"))
    jsMainImplementation(kotlin("stdlib"))
    jsMainImplementation("com.zegreatrob.jsmints:wdio-testing-library")
    jsMainImplementation("io.github.oshai:kotlin-logging")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-js")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-node")

    "jsPlaywrightTestImplementation"(platform("com.zegreatrob.jsmints:dependency-bom"))
    "jsPlaywrightTestImplementation"("com.zegreatrob.jsmints:playwright")
    "jsPlaywrightTestImplementation"("com.zegreatrob.testmints:async")

    "jsE2eTestImplementation"(platform("com.zegreatrob.jsmints:dependency-bom"))
    "jsE2eTestImplementation"("com.zegreatrob.jsmints:wdio-testing-library")
    "jsE2eTestImplementation"(kotlin("test"))
    "jsE2eTestImplementation"("com.zegreatrob.testmints:async")
    "jsE2eTestImplementation"("com.zegreatrob.testmints:minassert")
    "jsE2eTestImplementation"("org.jetbrains.kotlin-wrappers:kotlin-node")
}

dockerCompose {
    setProjectName("wdio-testing-library-test")
    containerLogToDir.set(project.file("build/test-output/containers-logs"))
    waitForTcpPorts.set(false)
}

dashboardTestSummary {
    reports.from(files(layout.buildDirectory.dir("test-results/e2e")).builtBy(tasks.named("e2eRun")))
}

tasks {
    e2eRun {
        outputs.cacheIf { true }
        inputs.files("docker-compose.yml", "Caddyfile")
        inputs.dir("src/jsE2eTest/resources")
        dependsOn(composeUp)
        finalizedBy(composeDown)
    }
    "kotlinNpmInstall" {
        dependsOn(gradle.includedBuild("libraries").task(":kotlinNpmInstall"))
    }
    "kotlinNodeJsSetup" {
        dependsOn(provider { gradle.includedBuild("libraries").task(":kotlinNodeJsSetup") })
        doNotTrackState(
            "The Kotlin-managed Node.js distribution can contain unreadable files Gradle cannot snapshot."
        )
    }
}
