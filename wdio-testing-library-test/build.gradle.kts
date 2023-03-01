plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.js2")
    id("com.zegreatrob.jsmints.plugins.wdiotest")
    alias(libs.plugins.com.avast.gradle.docker.compose)
}

wdioTest {
    includedBuild.set(true)
    wdioConfigFile.set(projectDir.resolve("wdio.conf.mjs"))
    useChrome.set(false)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":wdio-testing-library"))
    implementation("com.soywiz.korlibs.klock:klock")
    implementation("io.github.microutils:kotlin-logging")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-js")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-node")

    "e2eTestImplementation"(platform(project(":dependency-bom")))
    "e2eTestImplementation"(project(":wdio-testing-library"))
    "e2eTestImplementation"(kotlin("test"))
    "e2eTestImplementation"("com.zegreatrob.testmints:async")
    "e2eTestImplementation"("com.zegreatrob.testmints:minassert")
    "e2eTestImplementation"(jsconstraint("geckodriver"))
    "e2eTestImplementation"(jsconstraint("wdio-geckodriver-service"))
}

dockerCompose {
    setProjectName("wdio-testing-library-test")
    containerLogToDir.set(project.file("build/test-output/containers-logs"))
    waitForTcpPorts.set(false)
}

tasks {
    named("nodeTest") {
        enabled = false
    }
    named("test") {
        enabled = false
    }
    named("e2eRun") {
        dependsOn("composeUp")
    }
}
