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
    id("org.jmailen.kotlinter")
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
