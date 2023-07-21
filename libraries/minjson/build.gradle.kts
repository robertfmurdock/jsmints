plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

dependencies {
    commonMainImplementation("org.jetbrains.kotlin:kotlin-stdlib")
    commonMainImplementation("org.jetbrains.kotlin:kotlin-test")

    commonTestImplementation("com.zegreatrob.testmints:standard")
    commonTestImplementation("com.zegreatrob.testmints:minassert")
    commonTestImplementation("org.jetbrains.kotlin:kotlin-test")
}
