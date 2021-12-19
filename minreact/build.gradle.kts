plugins {
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.publish")
    id("com.zegreatrob.testmints.plugins.js")
}

kotlin {

    sourceSets {
        val jsMain by getting {
            dependencies {
                api(npm("core-js", "^3.6.5"))
                api("org.jetbrains.kotlin-wrappers:kotlin-react")
                api("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
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
