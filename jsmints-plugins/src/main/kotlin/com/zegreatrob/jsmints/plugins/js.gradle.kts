package com.zegreatrob.jsmints.plugins

import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("multiplatform")
    id("com.zegreatrob.jsmints.plugins.reports")
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    mavenCentral()
}

kotlin {
    js { nodejs { useCommonJs() } }
}

dependencies {
    "commonMainImplementation"(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.1"))
    "commonMainImplementation"(platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:0.0.1-pre.334"))
    "commonMainImplementation"(platform("com.zegreatrob.testmints:testmints-bom:7.3.3"))
}

ktlint {
    version.set("0.45.2")
}

tasks.withType(KotlinJsIrLink::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(Kotlin2JsCompile::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(KotlinJsTest::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.gradle.api.tasks.bundling.Jar::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.gradle.jvm.tasks.Jar::class).configureEach {
    outputs.cacheIf { true }
}
