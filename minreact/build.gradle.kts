plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-js-wrappers") }
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
                api("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.213-kotlin-1.5.10")
                api("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.213-kotlin-1.5.10")
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
