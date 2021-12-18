package com.zegreatrob.testmints.plugins

import org.gradle.kotlin.dsl.*
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
    }
}
