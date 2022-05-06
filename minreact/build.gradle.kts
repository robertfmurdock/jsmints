plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
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
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
    }
}
