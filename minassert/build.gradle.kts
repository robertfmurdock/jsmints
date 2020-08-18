import com.zegreatrob.testmints.build.BuildConstants

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "1.4.0"
}

repositories {
    mavenCentral()
}

kotlin {
    targets {
        jvm()
        js { nodejs {} }
        macosX64()
        linuxX64()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":mindiff"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test-common:${BuildConstants.kotlinVersion}")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-junit:${BuildConstants.kotlinVersion}")
            }
        }

        val nativeCommonMain by creating {
            dependsOn(commonMain)
        }

        val macosX64Main by getting { dependsOn(nativeCommonMain) }

        val linuxX64Main by getting { dependsOn(nativeCommonMain) }

        getByName("jsMain") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }
    }
}

tasks {
}



