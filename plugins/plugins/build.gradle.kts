import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.com.google.gson.JsonElement
import java.nio.charset.Charset
import java.util.*

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.com.gradle.plugin.publish)
//    alias(libs.plugins.org.jmailen.kotlinter)
    id("org.jetbrains.kotlin.jvm") version (embeddedKotlinVersion)
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
    implementation(libs.org.jetbrains.kotlin.kotlin.gradle.plugin)

    testImplementation(libs.junit)
}
gradlePlugin {
    website.set("https://github.com/robertfmurdock/jsmints")
    vcsUrl.set("https://github.com/robertfmurdock/jsmints")
    plugins {
        named("com.zegreatrob.jsmints.plugins.jspackage") {
            displayName = "Jsmints Js Package Plugin"
            description =
                "This plugin will load Javascript dependencies from a package.json file and apply them to a Kotlin JS gradle project."
            tags.addAll("javascript", "js", "package.json", "jsmints", "kotlin")
        }
        named("com.zegreatrob.jsmints.plugins.minreact") {
            displayName = "Jsmints Minreact Plugin"
            description =
                "This plugin will generate boilerplate related to working with React in Kotlin for function components and their props."
            tags.addAll("javascript", "js", "react", "minreact", "jsmints", "kotlin")
        }
        named("com.zegreatrob.jsmints.plugins.wrapper") {
            displayName = "Jsmints Wrapper Plugin"
            description =
                "This plugin will generate boilerplate for external types."
            tags.addAll("javascript", "js", "typescript", "jsmints", "kotlin")
        }
        named("com.zegreatrob.jsmints.plugins.ncu") {
            displayName = "Jsmints NCU Plugin"
            description =
                "This plugin provides tasks for using the npm-check-updates npm program, for updating package.json dependencies when using Kotlin JS."
            tags.addAll("javascript", "js", "package.json", "jsmints", "kotlin", "ncu", "npm-check-updates")
        }
        named("com.zegreatrob.jsmints.plugins.wdiotest") {
            displayName = "Jsmints WDIO Test Plugin"
            description =
                "This plugin adds support for using wdio.js with Kotlin JS, with some configuration conveniences."
            tags.addAll("javascript", "js", "wdio.js", "jsmints", "kotlin", "wdio", "webdriver")
        }
    }
}

testing {
    suites {
        register("functionalTest", JvmTestSuite::class) {
            gradlePlugin.testSourceSets(sources)
        }
    }
}

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks {
//    formatKotlinMain {
//        exclude { spec -> spec.file.absolutePath.contains("generated-sources") }
//    }
//    lintKotlinMain {
//        exclude { spec -> spec.file.absolutePath.contains("generated-sources") }
//    }
    val copyTemplates by registering(Copy::class) {
        inputs.property("version", rootProject.version)
        filteringCharset = "UTF-8"

        val gson: org.jetbrains.kotlin.com.google.gson.Gson = org.jetbrains.kotlin.com.google.gson.GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .create()

        val packageJson: JsonElement = gson.fromJson(
            rootDir.resolve("../libraries/dependency-bom/package.json")
                .readText(),
            JsonElement::class.java
        )

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
                )
            )
        }
        into(project.layout.buildDirectory.dir("generated-sources/templates/kotlin/main"))
    }
    compileKotlin {
        dependsOn(copyTemplates)
    }
    val projectResultPath = rootProject.layout.buildDirectory
        .dir("test-output/${project.path}/results".replace(":", "/"))
    val copyReportsToRootDirectory by creating(Copy::class) {
        mustRunAfter(check)
        from("build/reports")
        into(projectResultPath)
    }
    val copyTestResultsToRootDirectory by creating(Copy::class) {
        mustRunAfter(check)
        from("build/test-results")
        into(projectResultPath)
    }
    register("collectResults") {
        dependsOn(copyReportsToRootDirectory, copyTestResultsToRootDirectory)
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
}

fun JsonElement.dependency(name: String) = this.asJsonObject.getAsJsonObject("dependencies")
    .getAsJsonPrimitive(name)
    .asString
