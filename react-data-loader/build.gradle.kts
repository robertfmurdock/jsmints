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
            nodejs {
                testTask {
                    useMocha {
                        timeout = "20s"
                    }
                }
            }
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":minreact"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                api("org.jetbrains.kotlinx:atomicfu:0.18.0")
                api("org.jetbrains.kotlin:atomicfu:1.6.21")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(npm("@testing-library/react", "13.1.1"))
                implementation(npm("@testing-library/user-event", "14.1.1"))
                implementation(npm("jsdom", "19.0.0"))
                implementation(npm("global-jsdom", "8.4.0"))
                implementation("com.zegreatrob.testmints:async")
                implementation("com.zegreatrob.testmints:minassert")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
    }
}
