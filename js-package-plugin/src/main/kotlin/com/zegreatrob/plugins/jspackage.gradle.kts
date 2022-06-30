package com.zegreatrob.plugins

plugins {
    kotlin("js")
}

val toolsExtension = project.extensions.create("jstools", JsToolsExtension::class, loadPackageJson())

kotlin {
    js {
        useCommonJs()
        binaries.executable()
    }
}

dependencies {
    toolsExtension.packageJson.dependencies()?.forEach {
        implementation(npm(it.first, it.second.asText()))
    }
    toolsExtension.packageJson.devDependencies()?.forEach {
        testImplementation(npm(it.first, it.second.asText()))
    }
    implementation(enforcedPlatform("com.zegreatrob.testmints:testmints-bom:8.0.3"))
    implementation(enforcedPlatform("com.zegreatrob.jsmints:jsmints-bom:1.5.7"))
    implementation(enforcedPlatform("io.ktor:ktor-bom:2.0.3"))
    implementation(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:1.0.0-pre.348"))
    implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.3.3"))
    implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.3"))
}
