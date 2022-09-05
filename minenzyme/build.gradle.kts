import com.zegreatrob.jsmints.plugins.npmConstrained

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
                implementation(npmConstrained("enzyme"))
                implementation(npmConstrained("enzyme-adapter-react-16"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-legacy")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation("com.zegreatrob.testmints:standard")
            }
        }
    }
}
