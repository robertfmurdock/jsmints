plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

kotlin {
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("com.soywiz.korlibs.klock:klock:2.7.0")
                implementation("io.github.microutils:kotlin-logging:2.1.23")
                implementation(npm("@wdio/cli", "7.10.0"))
                implementation(npm("@wdio/local-runner", "7.10.0"))
            }
        }
    }
}
