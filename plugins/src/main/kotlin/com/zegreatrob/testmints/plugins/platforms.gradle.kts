package com.zegreatrob.testmints.plugins

import gradle.kotlin.dsl.accessors._07077b00ee1780ddc4a29d41420bd482.publishing
import gradle.kotlin.dsl.accessors._07077b00ee1780ddc4a29d41420bd482.signing
import org.gradle.kotlin.dsl.*
import java.nio.charset.Charset
import java.util.*

plugins {
    kotlin("multiplatform")
}

kotlin {
    targets {
        jvm()
        js { nodejs {} }
        macosX64()
        iosX64()
        linuxX64()
        mingwX64()
        linuxArm32Hfp()
    }
}
