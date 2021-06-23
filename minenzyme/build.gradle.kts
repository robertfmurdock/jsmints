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
                implementation(npm("enzyme", "^3.11.0"))
                implementation(npm("enzyme-adapter-react-16", "^1.15.2"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.213-kotlin-1.5.10")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(project(":standard"))
            }
        }
    }
}

tasks {
}
