package com.zegreatrob.jsmints.plugins

plugins {
    kotlin("js")
    id("com.zegreatrob.jsmints.plugins.reports")
    id("org.jmailen.kotlinter")
}

val jspackage = project.extensions.create<JsConstraintExtension>("jsconstraint")
configure<JsConstraintExtension> {
    json = File(rootDir, "../dependency-bom/package.json")
}
