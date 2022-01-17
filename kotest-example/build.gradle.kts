import com.zegreatrob.testmints.plugins.BuildConstants

plugins {
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.platforms")
    id("io.kotest.multiplatform") version "5.1.0"
}

kotlin {

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":standard"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test:${BuildConstants.kotlinVersion}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test:${BuildConstants.kotlinVersion}")
                implementation("io.kotest:kotest-framework-engine:5.1.0")
            }
        }

        val nativeCommonMain by creating {
            dependsOn(commonMain)
        }

        val macosX64Main by getting { dependsOn(nativeCommonMain) }

        val iosX64Main by getting { dependsOn(nativeCommonMain) }

        val linuxX64Main by getting { dependsOn(nativeCommonMain) }

        val mingwX64Main by getting { dependsOn(nativeCommonMain) }

        val jvmTest by getting {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5-jvm:5.1.0")
            }
        }
    }

}
