plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

kotlin {
    js {
        compilations.named("test") {
            packageJson { customField("mocha", mapOf("require" to "global-jsdom/register")) }
            nodejs { testTask(Action { useMocha { timeout = "20s" } } )}
        }
    }
}

dependencies {
    jsMainImplementation(kotlin("stdlib"))
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    jsMainImplementation(jsconstraint("@testing-library/react"))

    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    jsTestImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test")
    jsTestImplementation(jsconstraint("jsdom"))
    jsTestImplementation(jsconstraint("global-jsdom"))
}
