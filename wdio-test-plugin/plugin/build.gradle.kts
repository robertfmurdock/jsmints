import java.nio.charset.Charset
import java.util.*

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    alias(libs.plugins.com.gradle.plugin.publish)
    signing
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(gradleApi())

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
