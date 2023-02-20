import java.nio.charset.Charset
import java.util.*

plugins {
    `kotlin-dsl`
    kotlin("jvm")
    `java-gradle-plugin`
    alias(libs.plugins.com.gradle.plugin.publish)
    signing
    id("com.zegreatrob.jsmints.plugins.lint")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(gradleApi())
    implementation(libs.org.jetbrains.kotlin.kotlin.stdlib)
    implementation(libs.org.jetbrains.kotlin.kotlin.gradle.plugin)

    testImplementation(libs.junit)
}

group = "com.zegreatrob.jsmints"
version = "1.1.1"

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
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
    formatKotlinMain {
        exclude { spec -> spec.file.absolutePath.contains("generated-sources") }
    }
    lintKotlinMain {
        exclude { spec -> spec.file.absolutePath.contains("generated-sources") }
    }
}
val scmUrl = "https://github.com/robertfmurdock/jsmints"

gradlePlugin {
    website.set(scmUrl)
    vcsUrl.set(scmUrl)
}

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
