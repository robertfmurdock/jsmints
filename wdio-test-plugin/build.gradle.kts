import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

import de.gliderpilot.gradle.semanticrelease.GithubRepo
import de.gliderpilot.gradle.semanticrelease.SemanticReleaseChangeLogService
import de.gliderpilot.gradle.semanticrelease.SemanticReleaseInitialStateService
import de.gliderpilot.gradle.semanticrelease.SemanticReleaseStrategy
import org.ajoberstar.gradle.git.release.semver.ChangeScope

plugins {
    alias(libs.plugins.de.gliderpilot.semantic.release)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin)
    `maven-publish`
    signing
    base
    id("com.zegreatrob.jsmints.plugins.lint")
    id("com.zegreatrob.jsmints.plugins.versioning")
}

semanticRelease {
    changeLog(closureOf<SemanticReleaseChangeLogService> {
        changeScope = KotlinClosure1<org.ajoberstar.grgit.Commit, ChangeScope>({
            val version = extractVersion()
            when (version?.uppercase()) {
                "MAJOR" -> ChangeScope.MAJOR
                "MINOR" -> ChangeScope.MINOR
                "PATCH" -> ChangeScope.PATCH
                else -> null
            }
        })
    })
}

release {
    versionStrategy(
        semanticRelease.releaseStrategy.copyWith(
            mapOf(
                "selector" to de.gliderpilot.gradle.semanticrelease.SemanticReleaseStrategySelector { true },
                "createTag" to false
            )
        )
    )
}

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
    clean { delete(rootProject.buildDir) }
    check { dependsOn(provider { (getTasksByName("check", true) - this).toList() }) }
    create("formatKotlin") { dependsOn(provider { (getTasksByName("formatKotlin", true) - this).toList() }) }
    publish { dependsOn(provider { (getTasksByName("publish", true) - this).toList() }) }
}

fun org.ajoberstar.grgit.Commit.extractVersion(): String? {
    val open = fullMessage.indexOf("[")
    val close = fullMessage.indexOf("]")

    if (open < 0 || close < 0) {
        return null
    }
    return fullMessage.subSequence(open + 1, close).toString()
}
