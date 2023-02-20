package com.zegreatrob.jsmints.plugins

import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    js {
        nodejs {
            testTask { enabled = false }
        }
        useCommonJs()
        binaries.executable()
        compilations {
            val e2eTest by creating
            binaries.executable(e2eTest)
        }
    }
}

rootProject.extensions.findByType(NodeJsRootExtension::class.java).let {
    it?.nodeVersion = "19.6.0"
}

rootProject.yarn.ignoreScripts = false

val runnerConfiguration: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    runnerConfiguration("com.zegreatrob.jsmints:wdiorunner") {
        targetConfiguration = "executable"
    }
}

tasks {
    register("runWdio", Exec::class) {
        dependsOn(runnerConfiguration)

        val executable = runnerConfiguration.resolve().first()
        commandLine = listOf("node", executable.absolutePath)
        outputs.cacheIf { true }
    }
}
