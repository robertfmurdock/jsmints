package com.zegreatrob.jsmints.plugins

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

plugins {
    kotlin("multiplatform")
    id("com.zegreatrob.jsmints.plugins.reports")
    id("com.zegreatrob.jsmints.plugins.lint")
    id("com.zegreatrob.testmints.logs.mint-logs")
}

repositories {
    mavenCentral()
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        allWarningsAsErrors = true
    }
    js {
        nodejs { useCommonJs() }
    }
}

dependencies {
    "commonMainApi"(platform(project(":dependency-bom")))
}

val jspackage = project.extensions.create<JsConstraintExtension>("jsconstraint")
configure<JsConstraintExtension> {
    json = File(project(":dependency-bom").projectDir, "package.json")
}

rootProject.extensions.findByType(NodeJsRootExtension::class.java).let {
    if (it?.version != "21.5.0") {
        it?.version = "21.5.0"
    }
}
