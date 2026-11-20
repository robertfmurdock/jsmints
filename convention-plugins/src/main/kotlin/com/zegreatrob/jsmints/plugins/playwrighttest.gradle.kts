package com.zegreatrob.jsmints.plugins

import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBinaryMode
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

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
playwrightTest.reuseBrowser.convention(true)
playwrightTest.timeoutMillis.convention(60_000)
playwrightTest.configDirectory.convention(layout.projectDirectory.dir("playwright.conf.d"))
val browserOverride = providers.gradleProperty("playwrightBrowser")
val selectedProfile = providers.gradleProperty("playwrightProfile")
val baseUrlOverride = providers.gradleProperty("playwrightBaseUrl")
val headlessOverride = providers.gradleProperty("playwrightHeadless").map(String::toBooleanStrict)
val reuseOverride = providers.gradleProperty("playwrightReuseBrowser").map(String::toBooleanStrict)
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
    val engines = listOf(browserOverride.orElse(playwrightTest.browser).get()) +
        playwrightTest.profiles.map { it.browser.get() }
    args(engines.distinct().map(::supportedEngine))
}

fun supportedEngine(engine: String): String {
    require(engine == "chromium" || engine == "firefox" || engine == "webkit") { "Unsupported Playwright browser: $engine" }
    return engine
}

fun KotlinJsTest.configurePlaywrightRun(profile: PlaywrightProfile?) {
    val suiteTimeout = profile?.timeoutMillis?.orElse(playwrightTest.timeoutMillis)?.get()
        ?: playwrightTest.timeoutMillis.get()
    require(suiteTimeout > 0) { "Playwright test timeout must be positive" }
    useMocha { timeout = suiteTimeout.toString() }
    dependsOn(installBrowser, binary.linkSyncTask)
    inputFileProperty.set(binary.mainFileSyncPath)

    val profileName = profile?.name
    val engine = profile?.browser?.get() ?: browserOverride.orElse(playwrightTest.browser).get()
    val tracePath = profileName?.let { traceDirectory.map { directory -> directory.dir(it) } } ?: traceDirectory
    val configuration = playwrightTest.configDirectory.get()
    inputs.files(configuration.asFileTree).withPathSensitivity(PathSensitivity.RELATIVE)
    environment("JSMINTS_PLAYWRIGHT_CONFIG_DIRECTORY", configuration.asFile.absolutePath)
    environment("JSMINTS_PLAYWRIGHT_PROFILE", profileName ?: engine)
    environment("JSMINTS_PLAYWRIGHT_TRACE_DIRECTORY", tracePath.get().asFile.absolutePath)
    environment("JSMINTS_PLAYWRIGHT_RESOURCES", binary.mainFileSyncPath.get().asFile.parent)
    baseUrlOverride.orElse(profile?.baseUrl?.orElse(playwrightTest.baseUrl) ?: playwrightTest.baseUrl)
        .orNull?.let { environment("BASEURL", it) }
    environment("JSMINTS_PLAYWRIGHT_BROWSER", supportedEngine(engine))
    environment("JSMINTS_PLAYWRIGHT_HEADLESS", headlessOverride.orElse(profile?.headless?.orElse(playwrightTest.headless) ?: playwrightTest.headless).get().toString())
    environment("JSMINTS_PLAYWRIGHT_REUSE_BROWSER", reuseOverride.orElse(profile?.reuseBrowser?.orElse(playwrightTest.reuseBrowser) ?: playwrightTest.reuseBrowser).get().toString())
    doNotTrackState("Always exercise Playwright against the installed browser.")
}

target.nodejs {
    val defaultRun = testRuns.create("playwright") {
        setExecutionSourceFrom(compilation)
        executionTask.configure { configurePlaywrightRun(null) }
    }
    val runs = mutableMapOf<String, TaskProvider<KotlinJsTest>>()
    playwrightTest.profiles.all {
        val profile = this
        require(name.matches(Regex("[a-z][a-z0-9]*"))) { "Invalid Playwright profile name: $name" }
        browser.convention(name)
        val browserRun = testRuns.create("playwright${name.replaceFirstChar(Char::uppercaseChar)}") {
            setExecutionSourceFrom(compilation)
            executionTask.configure { configurePlaywrightRun(profile) }
        }
        runs[name] = browserRun.executionTask
    }

    val run = tasks.register("playwrightRun") {
        group = "verification"
        description = "Runs the configured Kotlin/JS Playwright browser profiles."
        dependsOn(
            provider {
                val selection = selectedProfile.orNull
                require(selection == null || !browserOverride.isPresent) {
                    "Choose either playwrightProfile or playwrightBrowser"
                }
                when {
                    selection != null -> listOf(requireNotNull(runs[selection]) { "Unknown Playwright profile: $selection" })
                    browserOverride.isPresent || runs.isEmpty() -> listOf(defaultRun.executionTask)
                    else -> runs.values.toList()
                }
            },
        )
    }
    tasks.named("check") {
        dependsOn(run)
    }
}
