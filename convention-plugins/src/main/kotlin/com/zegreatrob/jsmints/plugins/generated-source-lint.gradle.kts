package com.zegreatrob.jsmints.plugins

import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("org.jmailen.kotlinter")
}

tasks.withType<FormatTask>().configureEach {
    exclude { it.file.absolutePath.contains("generated") }
}
tasks.withType<LintTask>().configureEach {
    exclude { it.file.absolutePath.contains("generated") }
}
