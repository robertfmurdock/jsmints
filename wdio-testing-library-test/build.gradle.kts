plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.js2")
    id("com.zegreatrob.jsmints.plugins.wdiotest")
    alias(libs.plugins.com.avast.gradle.docker.compose)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
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

wdioTest {
    useChrome.set(true)
    chromeBinary.set(System.getenv("WDIO_CHROME_BINARY"))
    useHeadless.set(true)
    htmlReporter.set(true)
    allureReporter.set(true)
    allureReportHint.set("- link: http://localhost:63342/jsmints-root/wdio-testing-library-test/build/reports/e2e/allure/report/index.html")
    baseUrl.set("https://static.localhost")
}

dependencies {
    jsMainImplementation(platform("com.zegreatrob.jsmints:dependency-bom"))
    jsMainImplementation(kotlin("stdlib"))
    jsMainImplementation("com.zegreatrob.jsmints:wdio-testing-library")
    jsMainImplementation("com.soywiz.korlibs.klock:klock")
    jsMainImplementation("io.github.oshai:kotlin-logging")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-js")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-node")

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

tasks {
    e2eRun {
        dependsOn(composeUp)
        finalizedBy(composeDown)
    }
    "kotlinNpmInstall" {
        dependsOn(gradle.includedBuild("libraries").task(":kotlinNpmInstall"))
    }
    "kotlinNodeJsSetup" {
        dependsOn(provider { gradle.includedBuild("libraries").task(":kotlinNodeJsSetup") })
    }
}
