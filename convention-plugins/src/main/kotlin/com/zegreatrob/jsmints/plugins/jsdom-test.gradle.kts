package com.zegreatrob.jsmints.plugins

plugins {
    id("com.zegreatrob.jsmints.plugins.js")
}

kotlin.js {
    nodejs { testTask { useMocha { timeout = "20s" } } }
    compilations.named("test") {
        packageJson {
            customField("mocha", mapOf("require" to "global-jsdom/register"))
        }
    }
}

val constraint = extensions.getByType<JsConstraintExtension>()

dependencies {
    "jsTestImplementation"(constraint("jsdom"))
    "jsTestImplementation"(constraint("global-jsdom"))
}
