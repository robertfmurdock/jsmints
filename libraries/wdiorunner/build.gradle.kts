
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink

plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}

kotlin {
    js {
        nodejs {
            useCommonJs()
        }
        binaries.executable()
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
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-node")
    jsMainImplementation(jsconstraint("@wdio/cli"))
    jsMainImplementation(jsconstraint("@wdio/dot-reporter"))
    jsMainImplementation(jsconstraint("@wdio/junit-reporter"))
    jsMainImplementation(jsconstraint("@wdio/local-runner"))
    jsMainImplementation(jsconstraint("@wdio/mocha-framework"))
}

val executable: Configuration by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
    attributes { attribute(Attribute.of("com.zegreatrob.executable", String::class.java), "runner") }
}

val compileExecutableTask = tasks.named("compileProductionExecutableKotlinJs", KotlinJsIrLink::class)

tasks {
    val executableJar by registering(Jar::class) {
        dependsOn(compileExecutableTask)
        archiveClassifier.set("executable")
        from(compileExecutableTask.map { it.destinationDirectory.asFile })
    }

    artifacts.add(executable.name, executableJar) { builtBy(executableJar) }
    publishing.publications {
        withType<MavenPublication> { artifact(executableJar) }
    }
}
