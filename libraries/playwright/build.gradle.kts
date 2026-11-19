import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject

plugins {
    id("com.zegreatrob.jsmints.plugins.js")
}

dependencies {
    jsMainImplementation(jsconstraint("playwright"))
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    jsTestImplementation(kotlin("test"))
    jsTestImplementation("com.zegreatrob.testmints:async")
}

val node = NodeJsPlugin.apply(rootProject)
val playwrightCli = kotlin.js().compilations.getByName("test").npmProject.nodeModulesDir.map {
    it.file("playwright/cli.js")
}
val installSystemDependencies = providers.environmentVariable("RUNNER_OS").orNull == "Linux" &&
    providers.environmentVariable("GITHUB_ACTIONS").orNull == "true"

val installPlaywrightChromium = tasks.register<Exec>("installPlaywrightChromium") {
    group = "verification"
    description = "Installs the Chromium version required by the locked Playwright package."
    dependsOn(":kotlinNodeJsSetup", ":kotlinNpmInstall")
    executable = node.executable.get()
    args(playwrightCli.get().asFile, "install")
    if (installSystemDependencies) {
        args("--with-deps")
    }
    args("chromium")
}

tasks.named("jsNodeTest") {
    dependsOn(installPlaywrightChromium)
    doNotTrackState("Always exercise Playwright against the installed Chromium browser.")
}
