package com.zegreatrob.jsmints.plugins

import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

plugins {
    kotlin("multiplatform")
    id("com.zegreatrob.jsmints.plugins.reports")
    id("com.zegreatrob.jsmints.plugins.lint")
//    id("com.zegreatrob.testmints.logs.mint-logs")
}

repositories {
    mavenCentral()
}

kotlin {
    targets.all {
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = true
            }
        }
    }
    js { nodejs { useCommonJs() } }
}

dependencies {
    "commonMainApi"(platform(project(":dependency-bom")))
}

val jspackage = project.extensions.create<JsConstraintExtension>("jsconstraint")
configure<JsConstraintExtension> {
    json = File(project(":dependency-bom").projectDir, "package.json")
}

rootProject.extensions.findByType(NodeJsRootExtension::class.java).let {
    if (it?.nodeVersion != "19.6.0") {
        it?.nodeVersion = "19.6.0"
    }
}
