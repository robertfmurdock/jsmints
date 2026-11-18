package com.zegreatrob.jsmints.plugins

import org.gradle.api.Project

internal val jsKspConfigurations = setOf("kspJs", "kspTestJs", "kspJsTest")

internal fun Project.addJsmintsDependency(
    library: String,
    configurationNames: Set<String>,
    useLocalProject: () -> Boolean,
) {
    configurations.configureEach {
        if (name in configurationNames) {
            val dependency = providers.provider {
                if (useLocalProject()) {
                    project.dependencies.project(":$library")
                } else {
                    project.dependencies.create("com.zegreatrob.jsmints:$library:${PluginVersions.bomVersion}")
                }
            }
            dependencies.addLater(dependency)
        }
    }
}
