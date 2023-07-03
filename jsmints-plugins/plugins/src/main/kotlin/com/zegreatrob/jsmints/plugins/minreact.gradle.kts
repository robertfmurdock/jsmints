package com.zegreatrob.jsmints.plugins

plugins {
    base
    id("com.google.devtools.ksp")
}

afterEvaluate {
    dependencies {
        val processorDep = correctForLocal("minreact-processor")
        val minreactDep = correctForLocal("minreact")

        if (configurations.names.contains("kspJs")) {
            add("kspJs", processorDep)
        }
        if (configurations.names.contains("kspTestJs")) {
            add("kspTestJs", processorDep)
        }
        if (configurations.names.contains("kspJsTest")) {
            add("kspJsTest", processorDep)
        }
        if (configurations.names.contains("implementation")) {
            "implementation"(minreactDep)
        }
        if (configurations.names.contains("jsMainImplementation")) {
            "jsMainImplementation"(minreactDep)
        }
    }
}

fun correctForLocal(library: String): Any {
    val bomVersion: String = PluginVersions.bomVersion
    return if (bomVersion == "unspecified") {
        project(":$library")
    } else {
        "com.zegreatrob.jsmints:$library:${PluginVersions.bomVersion}"
    }
}
