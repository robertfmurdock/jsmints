plugins {
    kotlin("multiplatform")
}

kotlin {
    targets {
        js {
            nodejs {
                useCommonJs()
            }
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.4.2")
                implementation("com.soywiz.korlibs.klock:klock:1.12.0")
                implementation("io.github.microutils:kotlin-logging:2.0.6")
                implementation(npm("@wdio/cli", "7.0.8"))
            }
        }
    }
}
