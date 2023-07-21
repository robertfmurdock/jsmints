plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

kotlin {
    sourceSets {
        getByName("jsMain") {
            dependencies {
                api(project(":wdio"))
                implementation(kotlin("stdlib"))
                implementation("com.soywiz.korlibs.klock:klock")
                implementation("io.github.oshai:kotlin-logging")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation(jsconstraint("@testing-library/webdriverio"))
            }
        }
    }
}

tasks {
    named("jsNodeTest") {
        enabled = false
    }
    named("jsTestTestDevelopmentExecutableCompileSync") {
        enabled = false
    }
}
