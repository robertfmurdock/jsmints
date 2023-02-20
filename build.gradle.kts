import de.gliderpilot.gradle.semanticrelease.GithubRepo
import de.gliderpilot.gradle.semanticrelease.SemanticReleaseChangeLogService
import org.ajoberstar.gradle.git.release.semver.ChangeScope

plugins {
    alias(libs.plugins.com.github.sghill.distribution.sha)
    alias(libs.plugins.de.gliderpilot.semantic.release)
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    `maven-publish`
    signing
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

semanticRelease {
    changeLog(closureOf<SemanticReleaseChangeLogService> {

        repo(closureOf<GithubRepo> {
            setGhToken(System.getenv("GH_TOKEN"))
        })

        changeScope = KotlinClosure1<org.ajoberstar.grgit.Commit, ChangeScope>({
            val version = extractVersion()
            when (version?.toUpperCase()) {
                "MAJOR" -> ChangeScope.MAJOR
                "MINOR" -> ChangeScope.MINOR
                "PATCH" -> ChangeScope.PATCH
                else -> null
            }
        })
    })
}

tasks {
    val closeAndReleaseSonatypeStagingRepository by getting {
        mustRunAfter("publish")
    }
    check {
        dependsOn(gradle.includedBuilds.map { it.task(":check") })
    }
    create("formatKotlin") {
        dependsOn(gradle.includedBuilds.map { it.task(":formatKotlin") })
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
