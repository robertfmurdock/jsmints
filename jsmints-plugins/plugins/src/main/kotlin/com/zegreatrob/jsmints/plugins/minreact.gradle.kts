package com.zegreatrob.jsmints.plugins

import com.zegreatrob.jsmints.plugins.minreact.MinreactTestExtension

plugins {
    base
    id("com.google.devtools.ksp")
}

val minreact = project.extensions.create<MinreactTestExtension>("minreact")

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
    return if (minreact.includedBuild.get()) {
        project(":$library")
    } else {
        "com.zegreatrob.jsmints:$library:${PluginVersions.bomVersion}"
    }
}
