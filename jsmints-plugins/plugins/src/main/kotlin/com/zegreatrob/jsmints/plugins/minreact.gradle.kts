package com.zegreatrob.jsmints.plugins

plugins {
    base
    id("com.google.devtools.ksp")
}

afterEvaluate {
    dependencies {
        add("kspJs", project(":minreact-processor"))
        "jsMainImplementation"("com.zegreatrob.jsmints:minreact:${PluginVersions.bomVersion}")
        add("kspJsTest", project(":minreact-processor"))
    }
}
