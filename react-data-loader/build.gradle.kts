import com.zegreatrob.testmints.build.BuildConstants.coroutinesVersion

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
            nodejs {}
            useCommonJs()
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":minreact"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.274-kotlin-1.6.0")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.274-kotlin-1.6.0")
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
