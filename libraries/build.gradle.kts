plugins {
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin)
    alias(libs.plugins.com.github.sghill.distribution.sha)
    alias(libs.plugins.com.zegreatrob.tools.tagger)
    `maven-publish`
    signing
    id("com.zegreatrob.jsmints.plugins.versioning")
}

group = "com.zegreatrob.jsmints"

nexusPublishing {
    this@nexusPublishing.repositories {
        sonatype {
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            stagingProfileId.set("59331990bed4c")
        }
    }
}

tagger {
    releaseBranch = "master"
    githubReleaseEnabled.set(true)
}

tasks {
    calculateVersion {
        enabled = false
    }
    register("collectResults") {
        dependsOn(provider { (getTasksByName("collectResults", true) - this).toList() })
    }
    register("formatKotlin") {
        dependsOn(provider { (getTasksByName("formatKotlin", true) - this).toList() })
    }
    "versionCatalogUpdate" { dependsOn(":dependency-bom:ncuUpgrade") }
    release {
        mustRunAfter(check)
        finalizedBy(provider { (getTasksByName("publish", true)).toList() })
    }
    val closeAndReleaseSonatypeStagingRepository by getting {
        mustRunAfter(publish)
    }
    publish {
        mustRunAfter(check)
        dependsOn(provider { (getTasksByName("publish", true) - this).toList() })
        finalizedBy(closeAndReleaseSonatypeStagingRepository)
    }
}
