repositories {
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.5.0")
}

val kotlinVersion = "1.5.0"

dependencies {
    implementation(kotlin("stdlib", kotlinVersion))
}
