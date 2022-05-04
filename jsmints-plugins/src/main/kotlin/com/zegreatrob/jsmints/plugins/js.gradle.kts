package com.zegreatrob.jsmints.plugins

plugins {
    id("com.zegreatrob.jsmints.plugins.multiplatform")
    id("org.jmailen.kotlinter")
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
    "commonMainImplementation"(platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:0.0.1-pre.333"))
    "commonMainImplementation"(platform("com.zegreatrob.testmints:testmints-bom:7.3.3"))
}
