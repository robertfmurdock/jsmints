plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

kotlin {

    js {
        compilations.named("test") {
            packageJson {
                customField("mocha", mapOf("require" to "global-jsdom/register"))
            }
        }
    }

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
                implementation(npm("@testing-library/react", "13.1.1"))
                implementation(npm("jsdom", "19.0.0"))
                implementation(npm("global-jsdom", "8.4.0"))
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
            }
        }
    }
}
