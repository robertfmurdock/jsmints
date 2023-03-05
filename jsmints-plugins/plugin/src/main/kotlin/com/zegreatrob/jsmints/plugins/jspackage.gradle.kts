package com.zegreatrob.jsmints.plugins

import com.zegreatrob.jsmints.plugins.jspackage.JsPackageExtension
import com.zegreatrob.jsmints.plugins.jspackage.loadPackageJson
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin

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

NodeJsRootPlugin.apply(rootProject).apply {
    val libs = jspackage.dependencies()?.toMap()
    if (libs != null) {
        libs["webpack"]?.asText()?.let { versions.webpack.version = it }
        libs["webpack-cli"]?.asText()?.let { versions.webpackCli.version = it }
        libs["karma"]?.asText()?.let { versions.karma.version = it }
    }
}
