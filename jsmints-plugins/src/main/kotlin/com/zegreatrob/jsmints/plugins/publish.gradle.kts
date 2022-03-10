package com.zegreatrob.jsmints.plugins

import org.gradle.kotlin.dsl.*
import java.nio.charset.Charset
import java.util.*

plugins {
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

group = "com.zegreatrob.jsmints"

afterEvaluate {
    publishing.publications.withType<MavenPublication>().forEach {
        with(it) {
            val scmUrl = "https://github.com/robertfmurdock/jsmints"

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
                connection.set("git@github.com:robertfmurdock/jsmints.git")
                developerConnection.set("git@github.com:robertfmurdock/jsmints.git")
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project

    if (signingKey != null) {
        val decodedKey = Base64.getDecoder().decode(signingKey).toString(Charset.defaultCharset())
        useInMemoryPgpKeys(
            decodedKey,
            signingPassword
        )
    }
    sign(publishing.publications)
}


tasks {
    publish { finalizedBy("::closeAndReleaseSonatypeStagingRepository") }

    val javadocJar by creating(Jar::class) {
        archiveClassifier.set("javadoc")
        from("${rootDir.absolutePath}/javadocs")
    }
    publishing.publications {
        jvmPublication().withType<MavenPublication> {
            artifact(javadocJar)
        }

        val publishTasks = withType<AbstractPublishToMaven>()
        val nonMacPublications = nonMacPublications()
        if (isMacRelease()) {
            nonMacPublications.withType<MavenPublication> { publishTasks.disableTaskForPublication(this) }
        }
    }
}

fun Project.isSnapshot() = version.toString().contains("SNAPSHOT")

fun Project.isMacRelease() = findProperty("release-target") == "mac"

fun TaskCollection<AbstractPublishToMaven>.disableTaskForPublication(
    targetPub: MavenPublication
) {
    matching { it.publication == targetPub }
        .configureEach { this.onlyIf { false } }
}

fun PublicationContainer.nonMacPublications() = matching {
    it.name !in listOf(
        "macosX64",
        "iosX64",
        "iosArm32",
        "iosArm64"
    )
}

fun PublicationContainer.jvmPublication(): NamedDomainObjectSet<Publication> = matching { it.name == "jvm" }
