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

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

gradlePlugin {
    val scmUrl = "https://github.com/robertfmurdock/jsmints"
    website.set(scmUrl)
    vcsUrl.set(scmUrl)
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
