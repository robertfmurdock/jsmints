import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

repositories {
    maven { url = uri("https://plugins.gradle.org/m2/") }
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
    id("com.github.ben-manes.versions") version("0.42.0")
    id("java-gradle-plugin")
    id("se.patrikerdes.use-latest-versions") version("0.2.18")
}

val kotlinVersion = "1.7.10"

dependencies {
    implementation(kotlin("stdlib", kotlinVersion))
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation("com.github.ben-manes:gradle-versions-plugin:0.42.0")
    implementation("se.patrikerdes:gradle-use-latest-versions-plugin:0.2.18")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:10.3.0")
}

tasks {
    withType<DependencyUpdatesTask> {
        checkForGradleUpdate = true
        outputFormatter = "json"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"
        revision = "release"

        rejectVersionIf {
            "^[0-9.]+[0-9](-RC|-M[0-9]+|-RC[0-9]+|-beta.*|-Beta.*|-alpha.*)\$"
                .toRegex(RegexOption.IGNORE_CASE)
                .matches(candidate.version)
        }
    }
}
