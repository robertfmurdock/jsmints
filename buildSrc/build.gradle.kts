repositories {
    maven { url = uri("https://plugins.gradle.org/m2/") }
    mavenCentral()
}

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.6.0")
    `kotlin-dsl`
}

val kotlinVersion = "1.6.0"

dependencies {
    implementation(kotlin("stdlib", kotlinVersion))
    implementation("com.github.ben-manes:gradle-versions-plugin:0.39.0")
}
