plugins {
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
                implementation(npm("enzyme", "^3.11.0"))
                implementation(npm("enzyme-adapter-react-16", "^1.15.2"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.274-kotlin-1.6.0")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(project(":standard"))
            }
        }
    }
}
