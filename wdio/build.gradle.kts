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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion")
                implementation("com.soywiz.korlibs.klock:klock:2.0.7")
                implementation("io.github.microutils:kotlin-logging:2.0.6")
                implementation(npm("@wdio/cli", "7.0.8"))
            }
        }
    }
}
