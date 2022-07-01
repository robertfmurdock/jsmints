package com.zegreatrob.jsmints.plugins

import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec

plugins {
    id("com.zegreatrob.jsmints.plugins.jspackage")
}

val jspackage = extensions.getByName("jspackage") as JsPackageExtension

tasks {
    if(jspackage.exists) {
        kotlin.js().compilations.named("test").configure {
            val packageJson = "${project.projectDir.absolutePath}/package.json"
            NodeJsExec.create(this, "ncuUpgrade") {
                val nodeCommand = "ncu"
                val nodeCommandBin = "${project.nodeModulesDir}/.bin/$nodeCommand"
                nodeArgs.addAll(
                    "$nodeCommandBin -u --packageFile $packageJson --configFilePath $rootDir/.ncurc.json".split(" ")
                )
            }
        }
    }

}

dependencies {
    testImplementation(npm("npm-check-updates", "^15.0.0"))
}

val Project.nodeModulesDir get() = rootProject.buildDir.resolve("js/node_modules")
