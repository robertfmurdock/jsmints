import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("com.zegreatrob.jsmints.plugins.wrapper")
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.js")
    idea
}

kotlin {
    js {
        nodejs()
        compilations.named("test") {
            packageJson {
                customField("mocha", mapOf("require" to "global-jsdom/register"))
            }
        }
    }
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        allWarningsAsErrors = true
    }
    sourceSets.jsMain {
        kotlin.srcDir("build/generated/ksp/js/jsMain/kotlin")
    }
    sourceSets.jsTest {
        kotlin.srcDir("build/generated/ksp/js/jsTest/kotlin")
    }
}

wrapper {
    includedBuild.set(true)
}

dependencies {
    jsMainImplementation(kotlin("stdlib"))
    jsMainImplementation("io.github.oshai:kotlin-logging")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-js")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-node")

    jsTestImplementation("org.jetbrains.kotlin:kotlin-test")
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
}

tasks {
    withType<FormatTask> {
        dependsOn("kspKotlinJs")
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
    withType<LintTask> {
        dependsOn("kspKotlinJs")
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
}
