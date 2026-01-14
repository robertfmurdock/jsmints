import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("com.zegreatrob.jsmints.plugins.wrapper")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

wrapper {
    includedBuild.set(true)
}

dependencies {
    jsMainApi(project(":wdio"))
    jsMainImplementation(kotlin("stdlib"))
    jsMainImplementation("io.github.oshai:kotlin-logging")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-js")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation(jsconstraint("@sventschui/webdriverio-testing-library"))
}

tasks {
    named("jsNodeTest") {
        enabled = false
    }
    named("jsTestTestDevelopmentExecutableCompileSync") {
        enabled = false
    }
    withType<FormatTask> {
        dependsOn("kspKotlinJs")
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
    withType<LintTask> {
        dependsOn("kspKotlinJs")
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
}

