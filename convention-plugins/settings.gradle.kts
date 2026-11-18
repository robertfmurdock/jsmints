rootProject.name="jsmints-convention-plugins"

buildCache {
    local {
        isEnabled = true
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

val pluginPortalGroups = listOf("org.jmailen.gradle", "nl.littlerobots.vcu", "io.github.turansky.kfc")

dependencyResolutionManagement {
    repositories {
        mavenCentral {
            content {
                pluginPortalGroups.forEach { excludeGroup(it) }
            }
        }
        gradlePluginPortal {
            content {
                pluginPortalGroups.forEach { includeGroup(it) }
            }
        }
    }
    versionCatalogs {
        create("rootLibs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
