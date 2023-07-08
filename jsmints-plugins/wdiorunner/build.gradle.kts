
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

plugins {
    id("com.zegreatrob.jsmints.plugins.js2")
    id("com.zegreatrob.jsmints.plugins.lint")
    id("com.zegreatrob.jsmints.plugins.publish")
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
    jsMainImplementation(libs.io.github.microutils.kotlin.logging)
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

val executable: Configuration by configurations.creating

group = "com.zegreatrob.jsmints"

val compileExecutableTask = tasks.named("compileProductionExecutableKotlinJs", KotlinJsIrLink::class)

tasks {
    val executableJar by registering(Jar::class) {
        dependsOn(compileExecutableTask)
        archiveClassifier.set("executable")
        from(compileExecutableTask.map { it.destinationDirectory.asFile })
    }

    artifacts.add(executable.name, executableJar) {
        classifier = executable.name
        builtBy(executableJar)
    }

    publishing.publications {
        withType<MavenPublication> { artifact(executableJar) }
    }
}

rootProject.extensions.findByType(NodeJsRootExtension::class.java).let {
    it?.nodeVersion = "19.6.0"
}
