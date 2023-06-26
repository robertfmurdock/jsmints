plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.js")
    id("com.zegreatrob.jsmints.plugins.minreact")
}

kotlin {
    js { nodejs { useCommonJs() } }
}

dependencies {
    jsMainImplementation(kotlin("stdlib"))
    jsMainImplementation("io.github.microutils:kotlin-logging")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-js")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-node")

    jsTestImplementation(project(":react-testing-library"))
    jsTestImplementation(project(":user-event-testing-library"))
    jsTestImplementation(jsconstraint("jsdom"))
    jsTestImplementation(jsconstraint("global-jsdom"))
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test")
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
}
