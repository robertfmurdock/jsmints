import com.zegreatrob.testmints.plugins.BuildConstants.coroutinesVersion

plugins {
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.publish")
    id("com.zegreatrob.testmints.plugins.js")
}

kotlin {
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("com.soywiz.korlibs.klock:klock:2.1.0")
                implementation("io.github.microutils:kotlin-logging:2.1.10")
                implementation(npm("@wdio/cli", "7.10.0"))
                implementation(npm("@wdio/local-runner", "7.10.0"))
            }
        }
    }

}
