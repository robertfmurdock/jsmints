import com.zegreatrob.testmints.build.BuildConstants

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
    jcenter()
}

kotlin {
    targets {
        jvm()
        js { nodejs() }
        macosX64()
        linuxX64()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":standard"))
                api(project(":report"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-junit:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
            }
        }

        val nativeCommonMain by creating {
            dependsOn(commonMain)
            dependencies {
            }
        }

        val macosX64Main by getting { dependsOn(nativeCommonMain) }

        val linuxX64Main by getting { dependsOn(nativeCommonMain) }

        val jsMain by getting {
            dependencies {
                dependsOn(commonMain)
                implementation("org.jetbrains.kotlin:kotlin-test-js")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.9")
            }
        }

    }
}

tasks {

}
