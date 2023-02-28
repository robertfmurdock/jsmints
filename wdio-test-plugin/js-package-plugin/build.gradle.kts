import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.tools.ant.filters.ReplaceTokens
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
    api("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    implementation(kotlin("stdlib"))
    implementation(kotlin("gradle-plugin"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    val mapper = ObjectMapper()
    val ncuVersion = mapper.readTree(rootDir.resolve("../dependency-bom/package.json"))
        .at("/dependencies/npm-check-updates")
        .textValue()
    val copyTemplates by registering(Copy::class) {
        inputs.property("version", rootProject.version)
        filteringCharset = "UTF-8"
        from(project.projectDir.resolve("src/main/templates")) {
            filter<ReplaceTokens>(
                "tokens" to mapOf(
                    "NCU_VERSION" to ncuVersion,
                )
            )
        }
        into(project.buildDir.resolve("generated-sources/templates/kotlin/main"))
    }
    compileKotlin {
        dependsOn(copyTemplates)
    }

    sourceSets {
        main {
            java.srcDirs(copyTemplates)
        }
    }
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