package com.zegreatrob.jsmints.plugins

import org.gradle.api.file.FileTreeElement
import org.gradle.api.specs.Spec
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("org.jmailen.kotlinter")
}

val generatedSources = run {
    val generatedDirectories = listOf(
        layout.buildDirectory.dir("generated"),
        layout.buildDirectory.dir("generated-sources"),
    )
    Spec<FileTreeElement> { element ->
        val source = element.file.toPath().normalize()
        generatedDirectories.any { source.startsWith(it.get().asFile.toPath().normalize()) }
    }
}

tasks.withType<FormatTask>().configureEach {
    exclude(generatedSources)
}
tasks.withType<LintTask>().configureEach {
    exclude(generatedSources)
}
