pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    `gradle-enterprise`
}

rootProject.name = "jsmints"

include("dependency-bom")
include("jsmints-bom")
include("minjson")
include("minreact")
include("minreact-processor")
include("minenzyme")
include("react-data-loader")
include("wdio")
include("user-event-testing-library")
include("react-testing-library")
include("wdio-testing-library")
include("wdio-testing-library-test")
include("minreact-plugin-js-test")
include("minreact-plugin-mp-test")

includeBuild("jsmints-plugins")
includeBuild("jsmints-convention-plugins")

val isCiServer = System.getenv().containsKey("CI")

if (isCiServer) {
    gradleEnterprise {
        buildScan {
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
            tag("CI")
        }
    }
}

buildCache {
    local { isEnabled = true }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")