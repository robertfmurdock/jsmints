plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

dependencies {
    jsMainImplementation(project(":minreact"))
    jsMainImplementation(jsconstraint("enzyme"))
    jsMainImplementation(jsconstraint("enzyme-adapter-react-16"))
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-legacy")

    jsTestImplementation("com.zegreatrob.testmints:standard")
}