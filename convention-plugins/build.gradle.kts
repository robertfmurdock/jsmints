import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask
import nl.littlerobots.vcu.plugin.versionSelector

repositories {
    maven { url = uri("https://plugins.gradle.org/m2/") }
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    alias(libs.plugins.org.jmailen.kotlinter)
}

kotlin {
    compilerOptions {
        allWarningsAsErrors = true
    }
}

versionCatalogUpdate {
    val rejectRegex = "^[0-9.]+[0-9](-RC|-M[0-9]*|-RC[0-9]*.*|-beta.*|-Beta.*|-alpha.*|-dev.*)$".toRegex()
    versionSelector { versionCandidate ->
        !rejectRegex.matches(versionCandidate.candidate.version)
    }
}

dependencies {
    implementation(kotlin("stdlib", embeddedKotlinVersion))
    implementation(libs.org.jetbrains.kotlin.plugin.js.plain.objects)
    implementation(libs.org.jetbrains.kotlin.kotlin.gradle.plugin)
    implementation(libs.org.jmailen.gradle.kotlinter.gradle)
    implementation(libs.com.fasterxml.jackson.core.jackson.databind)
    implementation(libs.nl.littlerobots.vcu.plugin)
    implementation(platform(libs.com.zegreatrob.testmints.testmints.bom))
    implementation("com.zegreatrob.testmints:mint-logs-plugin")
}

tasks {
    withType(FormatTask::class) {
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
    withType(LintTask::class) {
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
    clean {
        delete(rootProject.layout.buildDirectory)
        dependsOn(provider { (getTasksByName("clean", true) - this).toList() })
    }
}

