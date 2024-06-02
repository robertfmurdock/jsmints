pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.gradle.develocity") version "3.17.4"
}

rootProject.name = "jsmints-root"

includeBuild("libraries")
includeBuild("plugins")
includeBuild("convention-plugins")
includeBuild("wdio-testing-library-test")


val isCiServer = System.getenv("CI").isNullOrBlank().not()

develocity {
    buildScan {
        publishing.onlyIf { isCiServer }
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        tag("CI")
    }
}

buildCache {
    local { isEnabled = true }
}
