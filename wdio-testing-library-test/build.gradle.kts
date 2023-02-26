plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.js")
    id("com.zegreatrob.jsmints.plugins.wdiotest")
    alias(libs.plugins.com.avast.gradle.docker.compose)
}

dependencies {
    jsMainImplementation(kotlin("stdlib"))
    jsMainImplementation(project(":wdio-testing-library"))
    jsMainImplementation("com.soywiz.korlibs.klock:klock")
    jsMainImplementation("io.github.microutils:kotlin-logging")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-js")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-node")

    "jsE2eTestImplementation"(platform(project(":dependency-bom")))
    "jsE2eTestImplementation"(project(":wdio-testing-library"))
    "jsE2eTestImplementation"(kotlin("test"))
    "jsE2eTestImplementation"("com.zegreatrob.testmints:async")
    "jsE2eTestImplementation"("com.zegreatrob.testmints:minassert")
    "jsE2eTestImplementation"(jsconstraint("wdio-html-nice-reporter"))
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
    named("e2eTest") {
        dependsOn("composeUp")
    }
}
