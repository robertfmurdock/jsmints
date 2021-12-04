import com.zegreatrob.testmints.plugins.BuildConstants

plugins {
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.publish")
    id("com.zegreatrob.testmints.plugins.platforms")
}

kotlin {

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test:${BuildConstants.kotlinVersion}")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(project(":standard"))
                implementation(project(":minassert"))
                implementation("org.jetbrains.kotlin:kotlin-test:${BuildConstants.kotlinVersion}")
            }
        }

        val nativeCommonMain by creating {
            dependsOn(commonMain)
        }

        val macosX64Main by getting { dependsOn(nativeCommonMain) }

        val linuxX64Main by getting { dependsOn(nativeCommonMain) }

    }
}
