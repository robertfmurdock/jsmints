import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import de.gliderpilot.gradle.semanticrelease.GithubRepo
import de.gliderpilot.gradle.semanticrelease.SemanticReleaseChangeLogService
import org.ajoberstar.gradle.git.release.semver.ChangeScope

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.20.0"
    id("de.gliderpilot.semantic-release") version "1.4.0"
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

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "com.jfrog.bintray")

    group = "com.zegreatrob.testmints"

    extensions.configure(BintrayExtension::class.java) {
        user = System.getenv("BINTRAY_USER")
        key = System.getenv("BINTRAY_KEY")
        override = true

        publish = true

        pkg(closureOf<BintrayExtension.PackageConfig> {
            repo = "zegreatrob"
            name = "testmints"

            version(closureOf<BintrayExtension.VersionConfig> {
            })
        })
    }

    val publishing = extensions.findByType(PublishingExtension::class.java)!!

    tasks {
        val bintrayUpload by getting(BintrayUploadTask::class) {

            doFirst {
                val publications = publishing.publications
                        .filterNot {
                            it.name.contains("-test")
                        }
                        .map { it.name }

                publishing.publications.getByName<MavenPublication>("kotlinMultiplatform") {
                    groupId = "com.zegreatrob.testmints"
                    artifactId = project.name
                    version = "${project.version}"
                }

                publishing.publications.filterIsInstance(MavenPublication::class.java)
                        .map {
                            it.artifact(file("build/publications/${it.name}/module.json")) {
                                extension = "module"
                            }
                        }

                publishing.publications.filterIsInstance(MavenPublication::class.java)
                        .map { it.artifacts }
                        .flatten()
                        .forEach { println("${it.file}") }

                setPublications(*publications.toTypedArray())
            }

            dependsOn("publishToMavenLocal")
        }

        val publish by getting {
            if (!isSnapshot()) {
                dependsOn(bintrayUpload)
            }
        }
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

fun Project.isSnapshot() = version.toString().contains("SNAPSHOT")