import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType

plugins {
    id("org.jetbrains.kotlin.multiplatform")  version "1.4.10"
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-js-wrappers") }
}

kotlin {

    targets {
        js(KotlinJsCompilerType.BOTH) {
            nodejs {}
            useCommonJs()
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":minreact"))
                implementation("org.jetbrains:kotlin-react:16.13.1-pre.110-kotlin-1.4.0")
                implementation("org.jetbrains:kotlin-react-dom:16.13.1-pre.110-kotlin-1.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0-M1")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(project(":async"))
                implementation(project(":minenzyme"))
                implementation(project(":minassert"))
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }
    }
}
