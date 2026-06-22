package com.zegreatrob.jsmints.plugins

repositories {
    mavenCentral()
}

tasks {
    val projectResultPath = rootProject.layout.buildDirectory.dir(
        "test-output/${project.path}/results".replace(":", "/"),
    )
    val check = named("check")
    val copyReportsToRootDirectory = register<Copy>("copyReportsToRootDirectory") {
        mustRunAfter(check)
        from("build/reports")
        into(projectResultPath)
    }
    val copyTestResultsToRootDirectory = register<Copy>("copyTestResultsToRootDirectory") {
        mustRunAfter(check)
        from("build/test-results")
        into(projectResultPath)
    }
    register("collectResults") {
        dependsOn(copyReportsToRootDirectory, copyTestResultsToRootDirectory)
    }
}

afterEvaluate {
    mkdir(rootProject.layout.buildDirectory.dir("test-output").get().asFile)
}
