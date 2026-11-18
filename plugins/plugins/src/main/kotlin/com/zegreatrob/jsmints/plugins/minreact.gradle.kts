package com.zegreatrob.jsmints.plugins

import com.zegreatrob.jsmints.plugins.minreact.MinreactTestExtension

plugins {
    base
    id("com.google.devtools.ksp")
}

val minreact = project.extensions.create<MinreactTestExtension>("minreact")

configurations.configureEach {
    val library = when (name) {
        "kspJs", "kspTestJs", "kspJsTest" -> "minreact-processor"
        "implementation", "jsMainImplementation" -> "minreact"
        else -> return@configureEach
    }
    dependencies.addLater(providers.provider {
        if (minreact.includedBuild.get()) {
            project.dependencies.project(":$library")
        } else {
            project.dependencies.create("com.zegreatrob.jsmints:$library:${PluginVersions.bomVersion}")
        }
    })
}
