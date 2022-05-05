package com.zegreatrob.jsmints.plugins

plugins {
    kotlin("js")
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
    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.1"))
    implementation(platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:0.0.1-pre.333"))
    implementation(platform("com.zegreatrob.testmints:testmints-bom:7.3.3"))
}
