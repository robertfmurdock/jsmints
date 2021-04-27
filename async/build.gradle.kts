import com.zegreatrob.testmints.build.BuildConstants
import com.zegreatrob.testmints.build.BuildConstants.coroutinesVersion
import com.zegreatrob.testmints.build.BuildConstants.kotlinVersion

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
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion")
                implementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion")
            }
        }

    }
}

tasks {

}
