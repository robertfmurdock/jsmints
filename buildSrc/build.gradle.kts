repositories {
    jcenter()
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.4.31")
}

val kotlinVersion = "1.4.31"

dependencies {
    implementation(kotlin("stdlib", kotlinVersion))
}
