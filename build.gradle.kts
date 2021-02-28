import com.jfrog.bintray.gradle.BintrayExtension
import de.gliderpilot.gradle.semanticrelease.GithubRepo
import de.gliderpilot.gradle.semanticrelease.SemanticReleaseChangeLogService
import org.ajoberstar.gradle.git.release.semver.ChangeScope
import java.nio.charset.Charset

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
    }
}

allprojects {
    apply(plugin = "se.patrikerdes.use-latest-versions")
    apply(plugin = "com.github.ben-manes.versions")
    repositories {
        mavenCentral()
        jcenter()
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
        maven { url = uri("https://dl.bintray.com/robertfmurdock/zegreatrob") }
        maven { url = uri("https://dl.bintray.com/kotlin/kotlin-js-wrappers") }
    }

    tasks {
        withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
            checkForGradleUpdate = true
            outputFormatter = "json"
            outputDir = "build/dependencyUpdates"
            reportfileName = "report"
            revision = "release"
        }
    }
}

plugins {
    id("se.patrikerdes.use-latest-versions") version "0.2.15"
    id("com.github.ben-manes.versions") version "0.36.0"
    id("de.gliderpilot.semantic-release") version "1.4.0"
    kotlin("multiplatform") version "1.4.31" apply false
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
    maven
    signing
}

group = "com.zegreatrob.testmints"

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
    if (isMacRelease()) {
        val updateGithubRelease by getting {
            enabled = false
        }
        val prepare by getting {
            enabled = false
        }
        val release by getting {
            enabled = false
        }
    }
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "com.jfrog.bintray")
    apply<SigningPlugin>()

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

    afterEvaluate {

        publishing.publications.withType<MavenPublication>().forEach {
            val publicationName = it.name

            with(it) {
                val scmUrl = "https://github.com/robertfmurdock/testmints"

                pom.name.set(project.name)
                pom.description.set(project.name)
                pom.url.set(scmUrl)

                pom.licenses {
                    license {
                        name.set("MIT License")
                        url.set(scmUrl)
                        distribution.set("repo")
                    }
                }
                pom.developers {
                    developer {
                        id.set("robertfmurdock")
                        name.set("Rob Murdock")
                        email.set("robert.f.murdock@gmail.com")
                    }
                }
                pom.scm {
                    url.set(scmUrl)
                    connection.set("git@github.com:robertfmurdock/testmints.git")
                    developerConnection.set("git@github.com:robertfmurdock/testmints.git")
                }
            }
        }
    }

    signing {
        val signingKey: String? by project
        val signingPassword: String? by project

        if (signingKey != null) {
            val decodedKey = java.util.Base64.getDecoder().decode(signingKey).toString(Charset.defaultCharset())
            useInMemoryPgpKeys(
                decodedKey,
                signingPassword
            )
        }
        sign(publishing.publications)
    }

    val macTargets = listOf(
        "macosX64",
        "iosX64",
        "iosArm32",
        "iosArm64"
    )

    if (isMacRelease()) {
        println("Disable attempt is scheduled")
        publishing.publications {
            matching { !macTargets.contains(it.name) }.all { targetPub ->
                println("disabling ${targetPub.name}")
                tasks.withType<AbstractPublishToMaven>()
                    .matching { it.publication == targetPub }
                    .configureEach { onlyIf { false } }
                true
            }
        }
    }

    tasks {
        val javadocJar by creating(Jar::class) {
            archiveClassifier.set("javadoc")
            from("${rootDir.absolutePath}/javadocs")
        }
        publishing.publications {
            matching { it.name == "jvm" }.withType<MavenPublication> {
                artifact(javadocJar)
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

fun Project.isMacRelease() = findProperty("release-target") == "mac"