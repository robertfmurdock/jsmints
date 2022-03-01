package com.zegreatrob.jsmints.plugins

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import gradle.kotlin.dsl.accessors._4ad077ad74816558e52d7069eb18a2f7.publish

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
            "^[0-9.]+[0-9](-RC|-M[0-9]+|-RC[0-9]+)\$"
                .toRegex()
                .matches(candidate.version)
        }
    }
}
