import com.zegreatrob.testmints.build.BuildConstants.coroutinesVersion

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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("com.soywiz.korlibs.klock:klock:2.1.0")
                implementation("io.github.microutils:kotlin-logging:2.0.8")
                implementation(npm("@wdio/cli", "7.0.8"))
            }
        }
    }
}
