plugins {
    id("com.zegreatrob.jsmints.plugins.generated-source-lint")
    id("com.zegreatrob.jsmints.plugins.wrapper")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

wrapper {
    includedBuild.set(true)
}

dependencies {
    jsMainApi(project(":wdio"))
    jsMainImplementation(kotlin("stdlib"))
    jsMainImplementation("io.github.oshai:kotlin-logging")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-js")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation(jsconstraint("@sventschui/webdriverio-testing-library"))
}

tasks {
    named("jsNodeTest") {
        enabled = false
    }
    named("jsTestTestDevelopmentExecutableCompileSync") {
        enabled = false
    }
}
