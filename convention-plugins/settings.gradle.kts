rootProject.name="jsmints-convention-plugins"

buildCache {
    local {
        isEnabled = true
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

dependencyResolutionManagement {
    repositories {
        mavenCentral {
            content {
                excludeGroup("org.jmailen.gradle")
                excludeGroup("nl.littlerobots.vcu")
                excludeGroup("io.github.turansky.kfc")
            }
        }
        gradlePluginPortal {
            content {
                includeGroup("org.jmailen.gradle")
                includeGroup("nl.littlerobots.vcu")
                includeGroup("io.github.turansky.kfc")
            }
        }
    }
    versionCatalogs {
        create("rootLibs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
