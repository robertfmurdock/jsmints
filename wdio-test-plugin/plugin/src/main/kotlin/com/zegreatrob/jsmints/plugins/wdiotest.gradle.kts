package com.zegreatrob.jsmints.plugins

repositories {
    mavenCentral()
}

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
