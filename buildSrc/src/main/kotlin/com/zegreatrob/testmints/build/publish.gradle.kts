package com.zegreatrob.testmints.build

import java.nio.charset.Charset
import java.util.*

plugins {
    `maven-publish`
    signing
}

group = "com.zegreatrob.testmints"

signing {
    val signingKey: String? by project
    val signingPassword: String? by project

    if (signingKey != null) {
        val decodedKey = Base64.getDecoder().decode(signingKey).toString(Charset.defaultCharset())
        useInMemoryPgpKeys(decodedKey, signingPassword)
    }
    sign(publishing.publications)
}

tasks {
    val javadocJar by creating(Jar::class) {
        archiveClassifier.set("javadoc")
        from("${rootDir.absolutePath}/javadocs")
    }

    publishing.publications {

        withType<MavenPublication> {
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

        jvmPublication().withType<MavenPublication> {
            artifact(javadocJar)
        }

        if (isMacRelease()) {
            val publishTasks = withType<AbstractPublishToMaven>()
            nonMacPublications().withType<MavenPublication> {
                publishTasks.matching { it.publication == this }
                    .configureEach { onlyIf { false } }
            }
        }

    }
}

fun Project.isSnapshot() = version.toString().contains("SNAPSHOT")

fun Project.isMacRelease() = findProperty("release-target") == "mac"

val macTargets = listOf(
    "macosX64",
    "iosX64",
    "iosArm32",
    "iosArm64"
)

fun PublicationContainer.nonMacPublications() = matching { !macTargets.contains(it.name) }

fun PublicationContainer.jvmPublication(): NamedDomainObjectSet<Publication> = matching { it.name == "jvm" }