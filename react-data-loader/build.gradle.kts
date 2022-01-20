plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

kotlin {

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":minreact"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(project(":minenzyme"))
                implementation("com.zegreatrob.testmints:async")
                implementation("com.zegreatrob.testmints:minassert")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
    }
}
