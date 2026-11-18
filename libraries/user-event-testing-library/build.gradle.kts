plugins {
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.jsdom-test")
}

dependencies {
    jsMainImplementation(kotlin("stdlib"))
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation(jsconstraint("@testing-library/user-event"))
    jsMainImplementation(jsconstraint("@testing-library/dom"))

    jsTestImplementation(project(":react-testing-library"))
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    jsTestImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test")
}
