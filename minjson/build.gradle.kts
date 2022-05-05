import com.zegreatrob.jsmints.plugins.BuildConstants

plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${BuildConstants.kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-test:${BuildConstants.kotlinVersion}")

    testImplementation("com.zegreatrob.testmints:standard")
    testImplementation("com.zegreatrob.testmints:minassert")
    testImplementation("org.jetbrains.kotlin:kotlin-test:${BuildConstants.kotlinVersion}")
}