package com.zegreatrob.jsmints.plugins

plugins {
    kotlin("js")
}

val jspackage = project.extensions.create("jspackage", JsPackageExtension::class, loadPackageJson())

dependencies {
    jspackage.dependencies()?.forEach {
        implementation(npm(it.first, it.second.asText()))
    }
    jspackage.devDependencies()?.forEach {
        testImplementation(npm(it.first, it.second.asText()))
    }
}
