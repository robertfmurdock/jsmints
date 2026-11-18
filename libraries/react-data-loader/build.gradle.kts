plugins {
    id("com.zegreatrob.jsmints.plugins.generated-source-lint")
    id("com.zegreatrob.jsmints.plugins.minreact")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.jsdom-test")
}

minreact {
    includedBuild.set(true)
}

dependencies {
    jsMainImplementation(project(":minreact"))
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    jsTestImplementation(project(":react-testing-library"))
    jsTestImplementation(project(":user-event-testing-library"))
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test")
}
