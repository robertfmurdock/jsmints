package com.zegreatrob.testmints.plugins

plugins {
    kotlin("multiplatform")
}

dependencies {
    "commonMainImplementation"(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.0"))
}
