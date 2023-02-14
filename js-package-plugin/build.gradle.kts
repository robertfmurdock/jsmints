import java.nio.charset.Charset
import java.util.*

repositories {
    maven { url = uri("https://plugins.gradle.org/m2/") }
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    id("org.jetbrains.kotlin.jvm")
    `kotlin-dsl`
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("java-gradle-plugin")
    alias(libs.plugins.com.gradle.plugin.publish)
    signing
}

group = "com.zegreatrob.jsmints"

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    api(platform(project(":dependency-bom")))
    api("com.fasterxml.jackson.core:jackson-databind")
    implementation(kotlin("stdlib"))
    implementation(kotlin("gradle-plugin"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val scmUrl = "https://github.com/robertfmurdock/jsmints"
afterEvaluate {
    publishing.publications.withType<MavenPublication>().forEach {
        with(it) {

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

gradlePlugin {
    website.set(scmUrl)
    vcsUrl.set(scmUrl)
    description = "These plugins are for interacting with package.json when using kotlin js."
}

tasks {
    publish { finalizedBy("::closeAndReleaseSonatypeStagingRepository") }
}
