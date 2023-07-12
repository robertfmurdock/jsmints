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
include("minenzyme")
include("minjson")
include("minreact")
include("minreact-plugin-test")
include("minreact-processor")
include("react-data-loader")
include("react-testing-library")
include("user-event-testing-library")
include("wdio")
include("wdio-testing-library")
//include("wdio-testing-library-test")
include("wdiorunner")

includeBuild("../plugins")
includeBuild("../convention-plugins")

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
