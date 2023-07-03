package com.zegreatrob.jsmints.plugins

plugins {
    base
    id("com.google.devtools.ksp")
}

afterEvaluate {
    dependencies {
        if (configurations.names.contains("kspJs")) {
            add("kspJs", project(":minreact-processor"))
        }
        if (configurations.names.contains("kspTestJs")) {
            add("kspTestJs", project(":minreact-processor"))
        }
        if (configurations.names.contains("kspJsTest")) {
            add("kspJsTest", project(":minreact-processor"))
        }
        if (configurations.names.contains("main")) {
            "mainImplementation"("com.zegreatrob.jsmints:minreact:${PluginVersions.bomVersion}")
        }
        if (configurations.names.contains("jsMain")) {
            "jsMainImplementation"("com.zegreatrob.jsmints:minreact:${PluginVersions.bomVersion}")
        }
    }
}
