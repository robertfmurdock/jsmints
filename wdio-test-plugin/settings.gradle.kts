rootProject.name = "wdio-test-plugin"

buildCache {
    local {
        isEnabled = true
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
includeBuild("../jsmints-plugins")
include(":plugin")
include(":wdiorunner")
include(":js-package-plugin")
