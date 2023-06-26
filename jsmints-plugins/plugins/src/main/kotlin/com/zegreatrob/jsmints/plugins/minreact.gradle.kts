package com.zegreatrob.jsmints.plugins

plugins {
    base
    id("com.google.devtools.ksp")
}

dependencies {
    add("kspJs", project(":minreact-processor"))
    add("kspJsTest", project(":minreact-processor"))
}
