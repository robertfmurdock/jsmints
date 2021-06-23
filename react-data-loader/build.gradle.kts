import com.zegreatrob.testmints.build.BuildConstants.coroutinesVersion

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-js-wrappers") }
}

kotlin {

    targets {
        js {
            nodejs {}
            useCommonJs()
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":minreact"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.213-kotlin-1.5.10")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.213-kotlin-1.5.10")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(project(":async"))
                implementation(project(":minenzyme"))
                implementation(project(":minassert"))
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
    }
}
