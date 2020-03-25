repositories {
    jcenter()
    maven { url = uri("https://plugins.gradle.org/m2/") }
    maven ("https://dl.bintray.com/kotlin/kotlin-eap")
}

plugins {
    id("org.jetbrains.kotlin.jvm").version ("1.4-M1")
}

val kotlinVersion = "1.4-M1"

dependencies {
    implementation(kotlin("stdlib", kotlinVersion))
}

