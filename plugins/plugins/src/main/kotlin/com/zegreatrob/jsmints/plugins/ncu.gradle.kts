package com.zegreatrob.jsmints.plugins

import com.zegreatrob.jsmints.plugins.jspackage.JsPackageExtension

plugins {
    id("com.zegreatrob.jsmints.plugins.jspackage")
}

val jspackage = extensions.getByName("jspackage") as JsPackageExtension

tasks {
    if (jspackage.exists) {
        kotlin.js().compilations.named("test").configure {
            register<NodeExec>("ncuUpgrade") {
                dependsOn("jsPublicPackageJson", ":kotlinNpmInstall")
                setup(this@configure)
                val packageJson = File(project.projectDir, "package.json")
                val nodeCommand = "ncu"
                val nodeCommandBin = "${project.nodeModulesDir}/.bin/$nodeCommand"
                val configFile = file("$rootDir/.ncurc.json")
                inputs.files(provider { listOf(configFile, packageJson).filter { it.exists() } })
                outputs.file(packageJson)

                arguments = listOf(
                    nodeCommandBin,
                    "-u",
                    "--packageFile",
                    packageJson.absolutePath,
                    "--configFilePath",
                    configFile.absolutePath,
                )
            }
        }
    }
}

dependencies {
    if (jspackage.exists) {
        "jsTestImplementation"(
            npm(
                "npm-check-updates",
                jspackage.dependencies()?.toMap()?.let { libs -> libs["npm-check-updates"]?.asText() }
                    ?: jspackage.devDependencies()?.toMap()?.let { libs -> libs["npm-check-updates"]?.asText() }
                    ?: PluginVersions.ncuVersion,
            ),
        )
    }
}

val Project.nodeModulesDir: String
    get() = rootProject.layout.buildDirectory
        .dir("js/node_modules").get().asFile.absolutePath
