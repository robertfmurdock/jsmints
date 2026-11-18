plugins {
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.jsdom-test")
}

dependencies {
    jsMainApi("org.jetbrains.kotlin-wrappers:kotlin-react")
    jsMainApi("org.jetbrains.kotlin-wrappers:kotlin-react-dom")

    jsTestImplementation(project(":react-testing-library"))
    jsTestImplementation(project(":user-event-testing-library"))
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test")
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
}
