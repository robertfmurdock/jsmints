package com.zegreatrob.jsmints.plugins

import com.zegreatrob.jsmints.plugins.wrapper.WrapperTestExtension

plugins {
    base
    id("com.google.devtools.ksp")
}

val wrapper = project.extensions.create<WrapperTestExtension>("wrapper")

configurations.configureEach {
    val library = when (name) {
        "kspJs", "kspTestJs", "kspJsTest" -> "wrapper-processor"
        else -> return@configureEach
    }
    val dependency = providers.provider {
        if (wrapper.includedBuild.get()) {
            project.dependencies.project(":$library")
        } else {
            project.dependencies.create("com.zegreatrob.jsmints:$library:${PluginVersions.bomVersion}")
        }
    }
    dependencies.addLater(dependency)
}
