package com.zegreatrob.jsmints.plugins

import java.nio.charset.Charset
import java.util.Base64

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
                    email.set("rob@continuousexcellence.io")
                }
            }
            pom.scm {
                url.set(scmUrl)
                connection.set("git@github.com:robertfmurdock/jsmints.git")
                developerConnection.set("git@github.com:robertfmurdock/jsmints.git")
            }
        }
    }

    tasks {
        val signKotlinMultiplatformPublication = findByName("signKotlinMultiplatformPublication")
        if (signKotlinMultiplatformPublication != null) {
            tasks.findByName("publishJsPublicationToSonatypeRepository")
                ?.dependsOn(signKotlinMultiplatformPublication)
            tasks.findByName("publishJvmPublicationToSonatypeRepository")
                ?.dependsOn(signKotlinMultiplatformPublication)
        }
        tasks.findByName("signJsPublication")
            ?.let {
                tasks.findByName("publishKotlinMultiplatformPublicationToSonatypeRepository")
                    ?.dependsOn(it)
            }
        tasks.findByName("signJvmPublication")
            ?.let {
                tasks.findByName("publishKotlinMultiplatformPublicationToSonatypeRepository")
                    ?.dependsOn(it)
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
            signingPassword,
        )
    }
    sign(publishing.publications)
}

tasks {
    publish { finalizedBy("::closeAndReleaseSonatypeStagingRepository") }

    val javadocJar by registering(Jar::class) {
        archiveClassifier.set("javadoc")
        from("${rootDir.absolutePath}/javadocs")
    }
    publishing.publications {
        withType<MavenPublication> { artifact(javadocJar) }
    }
}
