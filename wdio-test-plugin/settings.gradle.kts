rootProject.name="wdio-test-plugin"

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

    versionCatalogs {
//        create("libs") {
//            from(files("../gradle/libs.versions.toml"))
//        }
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
include(":plugin")
