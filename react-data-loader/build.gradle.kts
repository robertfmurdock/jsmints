plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

kotlin.js {
    nodejs { testTask { useMocha { timeout = "20s" } } }
    compilations.named("test") {
        packageJson { customField("mocha", mapOf("require" to "global-jsdom/register")) }
    }
}

dependencies {
    jsMainImplementation(project(":minreact"))
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    jsTestImplementation(project(":react-testing-library"))
    jsTestImplementation(jsconstraint("@testing-library/user-event"))
    jsTestImplementation(jsconstraint("jsdom"))
    jsTestImplementation(jsconstraint("global-jsdom"))
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test")
}
