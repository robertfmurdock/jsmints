package com.zegreatrob.testmints.plugins

import org.gradle.kotlin.dsl.*
import java.util.*

plugins {
    id("com.zegreatrob.testmints.plugins.multiplatform")
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

dependencies {
    "commonMainImplementation"(platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:0.0.1-pre.285-kotlin-1.6.10"))
}
