package com.zegreatrob.jsmints.plugins

plugins {
    id("com.zegreatrob.jsmints.plugins.jspackage")
}

val jspackage = extensions.getByName("jspackage") as JsPackageExtension

tasks {
    if (jspackage.exists) {
        kotlin.js().compilations.named("test").configure {

            register("ncuUpgrade", NodeExec::class) {
                setup(project)
                val packageJson = File(project.projectDir, "package.json")
                val nodeCommand = "ncu"
                val nodeCommandBin = "${project.nodeModulesDir}/.bin/$nodeCommand"
                val configFile = file("$rootDir/.ncurc.json")
                inputs.file(configFile)
                inputs.file(packageJson)
                outputs.file(packageJson)

                arguments = listOf(
                    nodeCommandBin,
                    "-u",
                    "--packageFile",
                    packageJson.absolutePath,
                    "--configFilePath",
                    configFile.absolutePath
                )
            }
        }
    }
}

dependencies {
    if (jspackage.exists) {
        testImplementation(npm("npm-check-updates",
            jspackage.dependencies()?.toMap()?.let { libs -> libs["npm-check-updates"]?.asText() }
                ?: jspackage.devDependencies()?.toMap()?.let { libs -> libs["npm-check-updates"]?.asText() }
                ?: "^15.0.0")
        )
    }
}

val Project.nodeModulesDir get() = rootProject.buildDir.resolve("js/node_modules")
