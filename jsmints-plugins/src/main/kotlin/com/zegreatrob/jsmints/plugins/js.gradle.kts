package com.zegreatrob.jsmints.plugins

import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmCachesSetup
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinPackageJsonTask
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
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
    "commonMainImplementation"(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4"))
    "commonMainImplementation"(platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:1.0.0-pre.373"))
    "commonMainImplementation"(platform("com.zegreatrob.testmints:testmints-bom:8.1.3"))
}

ktlint {
    version.set("0.45.2")
}

tasks.withType(org.jetbrains.kotlin.gradle.targets.js.npm.PublicPackageJsonTask::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(KotlinNpmCachesSetup::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(KotlinPackageJsonTask::class).configureEach {
    outputs.cacheIf { true }
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
tasks.withType(KotlinJvmTest::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.jetbrains.kotlin.gradle.plugin.mpp.TransformKotlinGranularMetadata::class).configureEach {
    outputs.cacheIf { true }
}
