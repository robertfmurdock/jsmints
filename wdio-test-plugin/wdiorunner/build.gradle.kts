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
    implementation(kotlin("stdlib"))
    implementation(platform(libs.org.jetbrains.kotlin.kotlin.bom))
    implementation(platform(libs.org.jetbrains.kotlinx.kotlinx.coroutines.bom))
    implementation(platform(libs.org.jetbrains.kotlin.wrappers.kotlin.wrappers.bom))
    implementation(libs.io.github.microutils.kotlin.logging)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-js")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-node")
    implementation(jsconstraint("@wdio/cli"))
    implementation(jsconstraint("@wdio/dot-reporter"))
    implementation(jsconstraint("@wdio/junit-reporter"))
    implementation(jsconstraint("@wdio/local-runner"))
    implementation(jsconstraint("@wdio/mocha-framework"))
}

val executable: Configuration by configurations.creating

group = "com.zegreatrob.jsmints"

artifacts {
    val task = tasks.named("compileProductionExecutableKotlinJs", KotlinJsIrLink::class)
    add(executable.name, task.map { it.outputFileProperty }) {
        builtBy(task)
    }
}

rootProject.extensions.findByType(NodeJsRootExtension::class.java).let {
    it?.nodeVersion = "19.6.0"
}