import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("com.zegreatrob.jsmints.plugins.minreact")
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

    targets.all {
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = false
            }
        }
    }
}

dependencies {
    jsMainImplementation(kotlin("stdlib"))
    jsMainImplementation(project(":minreact"))
    jsMainImplementation("io.github.microutils:kotlin-logging")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-js")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-node")

    jsTestImplementation(project(":react-testing-library"))
    jsTestImplementation(project(":user-event-testing-library"))
    jsTestImplementation(jsconstraint("jsdom"))
    jsTestImplementation(jsconstraint("global-jsdom"))
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test")
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
}

tasks {
    formatKotlinJsMain {
        dependsOn("kspKotlinJs")
    }
    formatKotlinJsTest {
        dependsOn("kspTestKotlinJs")
    }
    withType(LintTask::class) {
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }

    lintKotlinJsMain {
        dependsOn("kspKotlinJs")
    }
    lintKotlinJsTest {
        dependsOn("kspTestKotlinJs")
    }

}
