package com.zegreatrob.jsmints.plugins

plugins {
    kotlin("multiplatform")
    id("com.zegreatrob.jsmints.plugins.reports")
}

repositories {
    mavenCentral()
}

dependencies {
    "commonMainImplementation"(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.0"))
}
