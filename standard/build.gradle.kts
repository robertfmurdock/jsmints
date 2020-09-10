import com.zegreatrob.testmints.build.BuildConstants

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "1.4.10"
}

repositories {
    mavenCentral()
}

kotlin {
    targets {
        jvm()
        js { nodejs {} }
        macosX64()
        iosX64()
        linuxX64()
        mingwX64()
        linuxArm32Hfp()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":report"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common:${BuildConstants.kotlinVersion}")
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

        val iosX64Main by getting { dependsOn(nativeCommonMain) }

        val linuxX64Main by getting { dependsOn(nativeCommonMain) }

        val mingwX64Main by getting { dependsOn(nativeCommonMain) }

        val linuxArm32HfpMain by getting { dependsOn(nativeCommonMain) }

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



