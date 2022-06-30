plugins {
    `gradle-enterprise`
}

rootProject.name = "jsmints"
include("minjson")
include("minreact")
include("minenzyme")
include("react-data-loader")
include("wdio")
include("jsmints-bom")
//include("js-package-plugin")

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
