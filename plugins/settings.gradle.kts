rootProject.name = "jsmints-plugins"

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

include(":plugins")
