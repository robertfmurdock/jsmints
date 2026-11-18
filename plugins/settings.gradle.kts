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
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("androidx\\..*")
            }
        }
        mavenCentral {
            content {
                excludeGroupByRegex("com\\.android.*")
                excludeGroupByRegex("androidx\\..*")
            }
        }
    }
    versionCatalogs {
        create("rootLibs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

include(":plugins")
