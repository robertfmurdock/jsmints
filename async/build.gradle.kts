
import com.zegreatrob.testmints.build.BuildConstants

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "1.3.72"
}

repositories {
    mavenCentral()
    jcenter()
}

kotlin {
    targets {
        jvm()
        macosX64()
        linuxX64()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.4")
            }
        }

        val jvmMain by getting {
            dependencies  {
                implementation("org.jetbrains.kotlin:kotlin-test-junit:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.4")
            }
        }

        val nativeCommonMain by creating {
            dependsOn(commonMain)
            dependencies  {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:1.3.4")
            }
        }

        val macosX64Main by getting { dependsOn(nativeCommonMain) }

        val linuxX64Main by getting { dependsOn(nativeCommonMain) }


    }
}

tasks {

}
