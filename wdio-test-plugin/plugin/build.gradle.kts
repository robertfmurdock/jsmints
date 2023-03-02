import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.tools.ant.filters.ReplaceTokens
import java.nio.charset.Charset
import java.util.*

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.com.gradle.plugin.publish)
    id("com.zegreatrob.jsmints.plugins.lint")
    signing
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(gradleApi())
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    implementation(libs.org.jetbrains.kotlin.kotlin.stdlib)
    implementation(libs.org.jetbrains.kotlin.kotlin.gradle.plugin)

    testImplementation(libs.junit)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks {
    formatKotlinMain {
        exclude { spec -> spec.file.absolutePath.contains("generated-sources") }
    }
    lintKotlinMain {
        exclude { spec -> spec.file.absolutePath.contains("generated-sources") }
    }
    val copyTemplates by registering(Copy::class) {
        inputs.property("version", rootProject.version)
        filteringCharset = "UTF-8"
        val mapper = ObjectMapper()
        val packageJson = mapper.readTree(rootDir.resolve("../dependency-bom/package.json"))

        from(project.projectDir.resolve("src/main/templates")) {
            filter<ReplaceTokens>(
                "tokens" to mapOf(
                    "JSMINTS_BOM_VERSION" to rootProject.version,
                    "NCU_VERSION" to packageJson.dependency("npm-check-updates"),
                    "WDIO_NICE_HTML_REPORTER_VERSION" to packageJson.dependency("wdio-html-nice-reporter"),
                    "CHROMEDRIVER_VERSION" to packageJson.dependency("chromedriver"),
                    "WDIO_CHROMEDRIVER_SERVICE_VERSION" to packageJson.dependency("wdio-chromedriver-service"),
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

group = "com.zegreatrob.jsmints"

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

fun JsonNode.dependency(name: String) = at("/dependencies/$name")
    .textValue()