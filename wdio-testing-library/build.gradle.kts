import com.zegreatrob.jsmints.plugins.npmConstrained

plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

kotlin {
    sourceSets {
        val jsMain by getting {
            dependencies {
                api(project(":wdio"))
                implementation(kotlin("stdlib"))
                implementation("com.soywiz.korlibs.klock:klock")
                implementation("io.github.microutils:kotlin-logging")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation(npmConstrained("@wdio/cli"))
                implementation(npmConstrained("@wdio/local-runner"))
            }
        }
    }
}

tasks {
    named("jsNodeTest") {
        enabled = false
    }
}