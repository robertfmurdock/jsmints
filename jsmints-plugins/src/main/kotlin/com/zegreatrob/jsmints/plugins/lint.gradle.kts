package com.zegreatrob.jsmints.plugins

import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("org.jmailen.kotlinter")
}

tasks {
    withType(LintTask::class.java) {
        exclude("**/build/generated-sources/**")
    }
}
