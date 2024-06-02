pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.gradle.develocity") version "3.17.4"
}

rootProject.name = "jsmints"

include("dependency-bom")
include("jsmints-bom")
include("minenzyme")
include("minjson")
include("minreact")
include("minreact-plugin-test")
include("wrapper-plugin-test")
include("minreact-processor")
include("wrapper-processor")
include("react-data-loader")
include("react-testing-library")
include("user-event-testing-library")
include("wdio")
include("wdio-testing-library")
include("wdiorunner")

includeBuild("../plugins")
includeBuild("../convention-plugins")

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
