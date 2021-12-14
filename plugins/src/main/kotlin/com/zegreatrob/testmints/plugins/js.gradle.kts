package com.zegreatrob.testmints.plugins

import org.gradle.kotlin.dsl.*
import java.util.*

plugins {
    kotlin("multiplatform")
}

kotlin {
    targets {
        js {
            nodejs {
                useCommonJs()
            }
        }
    }
}
