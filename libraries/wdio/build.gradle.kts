import com.zegreatrob.jsmints.plugins.npmConstrained

plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

kotlin {
    sourceSets {
        jsMain {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("com.soywiz.korlibs.klock:klock")
                implementation("io.github.oshai:kotlin-logging")
                implementation(npmConstrained("@wdio/cli"))
                implementation(npmConstrained("webdriverio"))
            }
        }
    }
}
