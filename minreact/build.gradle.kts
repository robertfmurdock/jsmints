import com.zegreatrob.jsmints.plugins.npmConstrained

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
                implementation(npmConstrained("@testing-library/react"))
                implementation(npmConstrained("jsdom"))
                implementation(npmConstrained("global-jsdom"))
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
            }
        }
    }
}
