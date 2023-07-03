import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("com.zegreatrob.jsmints.plugins.minreact")
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.js2")
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
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/js/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/js/test/kotlin")
    }
}


minreact {
    includedBuild.set(true)
}

dependencies {
    implementation(platform(project(":dependency-bom")))
    implementation(kotlin("stdlib"))
    implementation("io.github.microutils:kotlin-logging")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-js")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-node")

    testImplementation(project(":react-testing-library"))
    testImplementation(project(":user-event-testing-library"))
    testImplementation(jsconstraint("jsdom"))
    testImplementation(jsconstraint("global-jsdom"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("com.zegreatrob.testmints:standard")
    testImplementation("com.zegreatrob.testmints:async")
    testImplementation("com.zegreatrob.testmints:minassert")
}

tasks {
    formatKotlinMain {
        dependsOn("kspKotlinJs")
    }
    withType(LintTask::class) {
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
    lintKotlinMain {
        dependsOn("kspKotlinJs")
    }
}
