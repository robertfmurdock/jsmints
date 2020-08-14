plugins {
    kotlin("multiplatform") version "1.3.72"
}

kotlin {
    targets {
        js {
            nodejs {}
            useCommonJs()
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.8")
                implementation("com.soywiz.korlibs.klock:klock:1.10.6")
                implementation("io.github.microutils:kotlin-logging-js:1.8.3")
                implementation(npm("@wdio/cli"))
            }
        }
    }

}
