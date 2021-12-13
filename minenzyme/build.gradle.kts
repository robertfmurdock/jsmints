plugins {
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.publish")
    id("com.zegreatrob.testmints.plugins.js")
}

kotlin {
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(npm("enzyme", "^3.11.0"))
                implementation(npm("enzyme-adapter-react-16", "^1.15.2"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.280-kotlin-1.6.0")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(project(":standard"))
            }
        }
    }
}
