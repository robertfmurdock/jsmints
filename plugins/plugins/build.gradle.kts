import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.tools.ant.filters.ReplaceTokens
import java.nio.charset.Charset
import java.util.Base64

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
    implementation(libs.com.fasterxml.jackson.core.jackson.databind)
    implementation(libs.com.google.devtools.ksp)
    implementation(libs.org.jetbrains.kotlin.kotlin.stdlib)
    implementation(libs.org.jetbrains.kotlin.kotlin.gradle.plugin)

    testImplementation(libs.junit)
}

testing {
    suites {
        register("functionalTest", JvmTestSuite::class) {
            gradlePlugin.testSourceSets(sources)
        }
    }
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
        val packageJson = mapper.readTree(rootDir.resolve("../libraries/dependency-bom/package.json"))

        from(project.projectDir.resolve("src/main/templates")) {
            filter<ReplaceTokens>(
                "tokens" to mapOf(
                    "JSMINTS_BOM_VERSION" to rootProject.version,
                    "NCU_VERSION" to packageJson.dependency("npm-check-updates"),
                    "WDIO_NICE_HTML_REPORTER_VERSION" to packageJson.dependency("wdio-html-nice-reporter"),
                    "WDIO_TIMELINE_REPORTER_VERSION" to packageJson.dependency("wdio-timeline-reporter"),
                    "WDIO_ALLURE_REPORTER_VERSION" to (packageJson.dependency("@wdio/allure-reporter")
                        ?: throw Exception("allure reporter")),
                    "ALLURE_CLI_VERSION" to packageJson.dependency("allure-commandline")!!,
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

fun JsonNode.dependency(name: String) = at("/dependencies/${name.replace("/", "~1")}")
    .textValue()