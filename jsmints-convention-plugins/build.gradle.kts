import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

repositories {
    maven { url = uri("https://plugins.gradle.org/m2/") }
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    alias(libs.plugins.com.github.ben.manes.versions)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    alias(libs.plugins.org.jmailen.kotlinter)
}

dependencies {
    implementation(libs.org.jetbrains.kotlin.kotlin.stdlib)
    implementation(libs.org.jetbrains.kotlin.kotlin.gradle.plugin)
    implementation(libs.com.github.ben.manes.gradle.versions.plugin)
    implementation(libs.org.jmailen.gradle.kotlinter.gradle)
    implementation(libs.com.fasterxml.jackson.core.jackson.databind)
    implementation(platform(libs.com.zegreatrob.testmints.testmints.bom))
    implementation("com.zegreatrob.testmints:mint-logs-plugin")
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
    formatKotlinMain {
        exclude { spec -> spec.file.absolutePath.contains("generated-sources") }
    }
    lintKotlinMain {
        exclude { spec -> spec.file.absolutePath.contains("generated-sources") }
    }
    clean {
        delete(rootProject.buildDir)
        dependsOn(provider { (getTasksByName("clean", true) - this).toList() })
    }
}

versionCatalogUpdate {
    sortByKey.set(true)
}
