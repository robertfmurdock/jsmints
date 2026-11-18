package com.zegreatrob.jsmints.plugins

import org.gradle.plugins.signing.Sign
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.nio.charset.Charset
import java.util.Base64

plugins {
    `maven-publish`
    signing
}

group = "com.zegreatrob.jsmints"

pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            allWarningsAsErrors = true
        }
    }
}

publishing.publications.withType<MavenPublication>().configureEach {
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

val signingTasks = tasks.withType<Sign>()
tasks.withType<PublishToMavenRepository>().configureEach {
    when (name) {
        "publishJsPublicationToSonatypeRepository", "publishJvmPublicationToSonatypeRepository" ->
            dependsOn(signingTasks.matching { it.name == "signKotlinMultiplatformPublication" })
        "publishKotlinMultiplatformPublicationToSonatypeRepository" ->
            dependsOn(signingTasks.matching { it.name == "signJsPublication" || it.name == "signJvmPublication" })
    }
}

signing {
    val signingKey = project.findProperty("signingKey") as String?
    val signingPassword = project.findProperty("signingPassword") as String?

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

    val javadocJar = register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        from("${rootDir.absolutePath}/javadocs")
    }
    publishing.publications {
        withType<MavenPublication> { artifact(javadocJar) }
    }
}
