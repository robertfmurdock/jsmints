plugins {
    id("com.zegreatrob.jsmints.plugins.generated-source-lint")
    id("com.zegreatrob.jsmints.plugins.wrapper")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.jsdom-test")
}

wrapper {
    includedBuild.set(true)
}

dependencies {
    jsMainImplementation(kotlin("stdlib"))
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    jsMainImplementation(jsconstraint("@testing-library/react"))

    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test")
}
