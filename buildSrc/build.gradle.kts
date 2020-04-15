repositories {
    jcenter()
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

plugins {
    id("org.jetbrains.kotlin.jvm").version ("1.3.72")
}

val kotlinVersion = "1.3.72"

dependencies {
    implementation(kotlin("stdlib", kotlinVersion))
}

