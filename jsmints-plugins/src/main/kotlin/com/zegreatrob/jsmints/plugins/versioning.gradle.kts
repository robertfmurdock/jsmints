package com.zegreatrob.jsmints.plugins

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("se.patrikerdes.use-latest-versions")
    id("com.github.ben-manes.versions")
}

repositories {
    mavenCentral()
}

tasks {
    withType<DependencyUpdatesTask> {
        checkForGradleUpdate = true
        outputFormatter = "json"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"
        revision = "release"

        rejectVersionIf {
            "^[0-9.]+[0-9](-RC|-M[0-9]+|-RC[0-9]+|-beta.*|-Beta.*|-alpha.*)\$"
                .toRegex(RegexOption.IGNORE_CASE)
                .matches(candidate.version)
        }
    }
}
