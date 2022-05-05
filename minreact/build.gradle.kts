plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

dependencies {
    api(npm("core-js", "^3.6.5"))
    api("org.jetbrains.kotlin-wrappers:kotlin-react")
    api("org.jetbrains.kotlin-wrappers:kotlin-react-dom")

    testImplementation(project(":minenzyme"))
    testImplementation("com.zegreatrob.testmints:standard")
    testImplementation("com.zegreatrob.testmints:minassert")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}
