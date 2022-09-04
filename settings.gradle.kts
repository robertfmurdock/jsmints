plugins {
    `gradle-enterprise`
}

rootProject.name = "jsmints"
include("dependency-bom")
include("js-package-plugin")
include("jsmints-bom")
include("minjson")
include("minreact")
include("minenzyme")
include("react-data-loader")
include("wdio")

includeBuild("jsmints-plugins")

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