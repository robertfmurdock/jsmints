package com.zegreatrob.jsmints.plugins

import com.zegreatrob.jsmints.plugins.jspackage.JsPackageExtension
import java.io.File

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
                val configFile = file("$rootDir/.ncurc.json")
                inputs.files(provider { listOf(configFile, packageJson).filter { it.exists() } })
                outputs.file(packageJson)
                nodeCommand = "ncu"

                arguments = listOf(
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
