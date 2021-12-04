import com.zegreatrob.testmints.build.BuildConstants

plugins {
    kotlin("multiplatform")
    id("com.zegreatrob.testmints.build.versioning")
    id("com.zegreatrob.testmints.build.publish")
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
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common:${BuildConstants.kotlinVersion}")
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
            }
        }
    }
}
