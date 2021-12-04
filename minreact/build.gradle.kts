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
        js {
            nodejs {
                useCommonJs()
            }
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                api(npm("core-js", "^3.6.5"))
                api("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.274-kotlin-1.6.0")
                api("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.274-kotlin-1.6.0")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(project(":minenzyme"))
                implementation(project(":standard"))
                implementation(project(":minassert"))
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
    }
}
