plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

tasks {
    named("jsNodeTest") {
        enabled = false
    }
    named("jsTestTestDevelopmentExecutableCompileSync") {
        enabled = false
    }
}

dependencies {
    jsMainApi(project(":wdio"))
    jsMainImplementation(kotlin("stdlib"))
    jsMainImplementation("com.soywiz.korlibs.klock:klock")
    jsMainImplementation("io.github.oshai:kotlin-logging")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation(jsconstraint("@testing-library/webdriverio"))
}
