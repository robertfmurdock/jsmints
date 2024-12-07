package com.zegreatrob.jsmints.plugins

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    id("com.zegreatrob.jsmints.plugins.reports")
//    id("org.jmailen.kotlinter")
}

repositories {
    mavenCentral()
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        allWarningsAsErrors = false
    }
}

val jspackage = project.extensions.create<JsConstraintExtension>("jsconstraint")
configure<JsConstraintExtension> {
    json = File(rootDir, "dependency-bom/package.json").let {
        if (it.exists()) {
            it
        } else {
            File(rootDir, "../dependency-bom/package.json")
        }
    }
}
