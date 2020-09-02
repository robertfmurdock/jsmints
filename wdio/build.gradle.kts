plugins {
    kotlin("multiplatform") version "1.4.0"
}

kotlin {
    targets {
        js(LEGACY) {
            nodejs {
                useCommonJs()
            }
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.9")
                implementation("com.soywiz.korlibs.klock:klock:1.12.0")
                implementation("io.github.microutils:kotlin-logging-js:1.8.3")
                implementation(npm("@wdio/cli", "6.4.0"))
            }
        }
    }
}
