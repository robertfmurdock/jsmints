package com.zegreatrob.jsmints.plugins

import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBinaryMode
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject

plugins {
    kotlin("multiplatform")
}

val target = kotlin.js()
val compilation = target.compilations.create("playwrightTest") {
    associateWith(target.compilations.getByName("main"))
    defaultSourceSet.dependencies {
        implementation(kotlin("test"))
    }
}

val binary = target.binaries.executable(compilation).single { it.mode == KotlinJsBinaryMode.DEVELOPMENT }

val playwrightTest = extensions.create<PlaywrightTestExtension>("playwrightTest")
playwrightTest.browser.convention("chromium")
playwrightTest.headless.convention(true)
playwrightTest.timeoutMillis.convention(60_000)
val fixtureBaseUrl = providers.gradleProperty("playwrightBaseUrl").orElse(playwrightTest.baseUrl)
val browser = providers.gradleProperty("playwrightBrowser").orElse(playwrightTest.browser)
val headless = providers.gradleProperty("playwrightHeadless").map(String::toBooleanStrict).orElse(playwrightTest.headless)
val installSystemDependencies = providers.gradleProperty("playwrightInstallSystemDeps").map(String::toBooleanStrict).orElse(false)
val node = NodeJsPlugin.apply(rootProject)
val npmProject = compilation.npmProject
val traceDirectory = layout.buildDirectory.dir("reports/playwright/traces")
val installBrowser = tasks.register<Exec>("installPlaywrightBrowser") {
    group = "verification"
    description = "Installs the browser required by the locked Playwright package."
    dependsOn(":kotlinNodeJsSetup", ":kotlinNpmInstall")
    executable = node.executable.get()
    args(npmProject.nodeModulesDir.get().file("playwright/cli.js").asFile, "install")
    if (installSystemDependencies.get()) {
        args("--with-deps")
    }
    val engine = browser.get()
    require(engine == "chromium" || engine == "firefox") { "Unsupported Playwright browser: $engine" }
    args(engine)
}

target.nodejs {
    val playwrightRun = testRuns.create("playwright") {
        setExecutionSourceFrom(compilation)
        executionTask.configure {
            val suiteTimeout = playwrightTest.timeoutMillis.get()
            require(suiteTimeout > 0) { "Playwright test timeout must be positive" }
            useMocha { timeout = suiteTimeout.toString() }
            dependsOn(installBrowser, binary.linkSyncTask)
            inputFileProperty.set(binary.mainFileSyncPath)
            environment("JSMINTS_PLAYWRIGHT_TRACE_DIRECTORY", traceDirectory.get().asFile.absolutePath)
            environment("JSMINTS_PLAYWRIGHT_RESOURCES", binary.mainFileSyncPath.get().asFile.parent)
            fixtureBaseUrl.orNull?.let { environment("BASEURL", it) }
            environment("JSMINTS_PLAYWRIGHT_BROWSER", browser.get())
            environment("JSMINTS_PLAYWRIGHT_HEADLESS", headless.get().toString())
            doNotTrackState("Always exercise Playwright against the installed browser.")
        }
    }
    val run = tasks.register("playwrightRun") {
        group = "verification"
        description = "Runs the Kotlin/JS Playwright end-to-end suite."
        dependsOn(playwrightRun.executionTask)
    }
    tasks.named("check") {
        dependsOn(run)
    }
}
