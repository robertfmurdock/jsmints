package com.zegreatrob.jsmints.plugins

import org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension

plugins {
    kotlin("js")
    id("com.zegreatrob.jsmints.plugins.reports")
    id("org.jmailen.kotlinter")
}

repositories {
    mavenCentral()
}

kotlin(fun KotlinJsProjectExtension.() {
    js {
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = false
            }
        }
    }
})

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
