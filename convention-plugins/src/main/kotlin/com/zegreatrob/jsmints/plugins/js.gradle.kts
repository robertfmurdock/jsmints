package com.zegreatrob.jsmints.plugins

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec

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
        nodejs {
            useEsModules()
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            compilerOptions { target = "es2015" }
        }
    }
}

dependencies {
    "commonMainApi"(platform(project(":dependency-bom")))
}

project.extensions.create<JsConstraintExtension>("jsconstraint")

configure<JsConstraintExtension> {
    json = File(project(":dependency-bom").projectDir, "package.json")
}

rootProject.extensions.findByType(NodeJsEnvSpec::class.java).let {
    it?.version = "24.7.0"
}
