
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBinaryMode

plugins {
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

kotlin {
    js {
        nodejs {
            useCommonJs()
        }
    }
}

dependencies {
    jsMainImplementation(kotlin("stdlib"))
    jsMainImplementation(platform(libs.org.jetbrains.kotlin.kotlin.bom))
    jsMainImplementation(platform(libs.org.jetbrains.kotlinx.kotlinx.coroutines.bom))
    jsMainImplementation(platform(libs.org.jetbrains.kotlin.wrappers.kotlin.wrappers.bom))
    jsMainImplementation(libs.io.github.oshai.kotlin.logging)
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-js")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-node")
    jsMainImplementation(jsconstraint("@wdio/cli"))
    jsMainImplementation(jsconstraint("@wdio/dot-reporter"))
    jsMainImplementation(jsconstraint("@wdio/junit-reporter"))
    jsMainImplementation(jsconstraint("@wdio/local-runner"))
    jsMainImplementation(jsconstraint("@wdio/mocha-framework"))
}

val executable = configurations.create("executable") {
    isCanBeResolved = false
    isCanBeConsumed = true
    attributes { attribute(Attribute.of("com.zegreatrob.executable", String::class.java), "runner") }
}

val productionBinary = kotlin.js().binaries.executable()
    .single { it.mode == KotlinJsBinaryMode.PRODUCTION }

tasks {
    val executableJar = register<Jar>("executableJar") {
        archiveClassifier.set("executable")
        from(productionBinary.linkTask.flatMap { it.destinationDirectory })
    }

    artifacts.add(executable.name, executableJar) { builtBy(executableJar) }
    publishing.publications {
        withType<MavenPublication> { artifact(executableJar) }
    }
}
