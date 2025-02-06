plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

kotlin.js().compilations.named("test") {
    packageJson {
        customField("mocha", mapOf("require" to "global-jsdom/register"))
    }
}

dependencies {
    jsMainApi("org.jetbrains.kotlin-wrappers:kotlin-react")
    jsMainApi("org.jetbrains.kotlin-wrappers:kotlin-react-dom")

    jsTestImplementation(project(":react-testing-library"))
    jsTestImplementation(project(":user-event-testing-library"))
    jsTestImplementation(jsconstraint("jsdom"))
    jsTestImplementation(jsconstraint("global-jsdom"))
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test")
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
}
