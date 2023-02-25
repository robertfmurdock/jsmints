plugins {
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin)
    `maven-publish`
    signing
    id("com.zegreatrob.jsmints.plugins.derp")
    id("com.zegreatrob.jsmints.plugins.js")
    id("com.zegreatrob.jsmints.plugins.versioning")
}

group = "com.zegreatrob.jsmints"

nexusPublishing {
    repositories {
        sonatype {
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            stagingProfileId.set("59331990bed4c")
        }
    }
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
        dependsOn(gradle.includedBuild("wdio-test-plugin").task(":publish"))
        finalizedBy(closeAndReleaseSonatypeStagingRepository)
    }
    check {
        dependsOn(gradle.includedBuilds.map { it.task(":check") })
    }
    "formatKotlin" {
        dependsOn(gradle.includedBuilds.map { it.task(":formatKotlin") })
    }
    "kotlinNpmInstall" {
        dependsOn(provider {
            gradle.includedBuild("wdio-test-plugin").task(":check")
        })
    }
}

fun org.ajoberstar.grgit.Commit.extractVersion(): String? {
    val open = fullMessage.indexOf("[")
    val close = fullMessage.indexOf("]")

    if (open < 0 || close < 0) {
        return null
    }
    return fullMessage.subSequence(open + 1, close).toString()
}
