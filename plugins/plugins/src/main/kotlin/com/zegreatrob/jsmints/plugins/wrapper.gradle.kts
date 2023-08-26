package com.zegreatrob.jsmints.plugins

import com.zegreatrob.jsmints.plugins.wrapper.WrapperTestExtension

plugins {
    base
    id("com.google.devtools.ksp")
}

val wrapper = project.extensions.create<WrapperTestExtension>("wrapper")

afterEvaluate {

    dependencies {
        val processorDep = correctForLocal("wrapper-processor")

        if (configurations.names.contains("kspJs")) {
            add("kspJs", processorDep)
        }
        if (configurations.names.contains("kspTestJs")) {
            add("kspTestJs", processorDep)
        }
        if (configurations.names.contains("kspJsTest")) {
            add("kspJsTest", processorDep)
        }
    }
}

fun correctForLocal(library: String): Any {
    return if (wrapper.includedBuild.get()) {
        project(":$library")
    } else {
        "com.zegreatrob.jsmints:$library:${PluginVersions.bomVersion}"
    }
}
