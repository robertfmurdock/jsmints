import com.zegreatrob.testmints.build.BuildConstants

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    targets {
        js {
            nodejs {}
            useCommonJs()
        }
    }

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
    }
}

tasks {
}
