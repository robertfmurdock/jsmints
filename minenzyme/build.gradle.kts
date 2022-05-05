plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

dependencies {
    implementation(npm("enzyme", "^3.11.0"))
    implementation(npm("enzyme-adapter-react-16", "^1.15.2"))
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-legacy")
    implementation(project(":minreact"))

    testImplementation("com.zegreatrob.testmints:standard")
}