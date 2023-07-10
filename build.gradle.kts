plugins {
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin)
    alias(libs.plugins.com.github.sghill.distribution.sha)
    alias(libs.plugins.com.zegreatrob.tools.tagger)
    `maven-publish`
    signing
    id("com.zegreatrob.jsmints.plugins.js")
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
}

group = "com.zegreatrob.jsmints"

nexusPublishing {
    this@nexusPublishing.repositories {
        sonatype {
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            stagingProfileId.set("59331990bed4c")
        }
    }
}

tagger {
    releaseBranch = "master"
    githubReleaseEnabled.set(true)
}

tasks {
    val closeAndReleaseSonatypeStagingRepository by getting {
        mustRunAfter(publish)
    }
    release {
        mustRunAfter(check)
        finalizedBy(provider { (getTasksByName("publish", true)).toList() })
    }

    publish {
        mustRunAfter(check)
        dependsOn(provider { (getTasksByName("publish", true) - this).toList() })
        finalizedBy(closeAndReleaseSonatypeStagingRepository)
    }
    val jsmintsPluginsBuild = gradle.includedBuild("jsmints-plugins")
    val jsmintsConventionPluginsBuild = gradle.includedBuild("jsmints-convention-plugins")
    val jsmintsBuilds = listOf(jsmintsPluginsBuild, jsmintsConventionPluginsBuild)

    check {
        dependsOn(provider { (getTasksByName("check", true) - this).toList() })
        dependsOn(provider { jsmintsBuilds.map { it.task(":check") } })
    }
    clean {
        dependsOn(provider { jsmintsBuilds.map { it.task(":clean") } })
    }
    "formatKotlin" {
        dependsOn(provider { jsmintsBuilds.map { it.task(":formatKotlin") } })
    }
    "versionCatalogUpdate" {
        dependsOn(provider { jsmintsBuilds.map { it.task(":versionCatalogUpdate") } })
    }
    "kotlinNpmInstall" {
        dependsOn(provider {
            jsmintsPluginsBuild.task(":kotlinNpmInstall")
        })
    }
    "kotlinNodeJsSetup" {
        dependsOn(provider {
            jsmintsPluginsBuild.task(":kotlinNodeJsSetup")
        })
    }
    "kotlinUpgradeYarnLock" {
        dependsOn(provider {
            jsmintsPluginsBuild.task(":kotlinUpgradeYarnLock")
        })
    }
}
