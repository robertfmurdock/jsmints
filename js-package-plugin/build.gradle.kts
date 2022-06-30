repositories {
    maven { url = uri("https://plugins.gradle.org/m2/") }
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    id("org.jetbrains.kotlin.jvm")
    `kotlin-dsl`
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.gradle.plugin-publish") version "1.0.0"
}

val kotlinVersion = "1.7.0"

dependencies {
    implementation(kotlin("stdlib", kotlinVersion))
    implementation(kotlin("gradle-plugin", kotlinVersion))
    api("com.fasterxml.jackson.core:jackson-databind:2.13.3")
}

//pluginBundle {
//    website = REPO_URL
//    vcsUrl = REPO_URL
//
//    pluginTags = KfcPlugin.values()
//        .associate { it.pluginName to it.tags }
//}
