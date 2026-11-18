package com.zegreatrob.jsmints.plugins

plugins {
    id("com.zegreatrob.jsmints.plugins.publish")
    kotlin("multiplatform")
}

kotlin {
    jvmToolchain(21)
    jvm()
}

val processorLibraries = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    "jvmMainImplementation"(platform(project(":dependency-bom")))
    "jvmMainImplementation"("com.squareup:kotlinpoet-ksp")
    "jvmMainImplementation"(processorLibraries.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
}
