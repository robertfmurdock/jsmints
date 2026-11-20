package com.zegreatrob.wrapper.playwright

import kotlinx.coroutines.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.js.JsModule
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

internal class PlaywrightRunConfiguration(private val fragments: List<ConfigFragment> = emptyList()) {
    fun launchOptions(headless: Boolean?): Json {
        val defaults = json()
        headless?.let { defaults["headless"] = it }
        return fragments.fold(defaults) { options, fragment ->
            fragment.launchOptions?.let { options.add(it) } ?: options
        }
    }

    fun contextOptions(baseUrl: String?): Json {
        val defaults = fragments.fold(json()) { options, fragment ->
            fragment.contextOptions?.let { options.add(it) } ?: options
        }
        baseUrl?.let { defaults["baseURL"] = it }
        return defaults
    }

    suspend fun afterLaunch(browser: JsBrowser) = runHooks("afterLaunch", browser) { afterLaunch }
    suspend fun afterContext(context: JsBrowserContext) = runHooks("afterContext", context) { afterContext }
    suspend fun afterPage(page: JsPage) = runHooks("afterPage", page) { afterPage }

    private suspend fun runHooks(name: String, value: Any, select: ConfigFragment.() -> NativeHook?) {
        fragments.forEach { fragment ->
            val hook = fragment.select() ?: return@forEach
            try {
                Promise.resolve(hook(value)).await()
            } catch (error: Throwable) {
                throw IllegalStateException("Playwright $name hook failed in ${fragment.filename}", error)
            }
        }
    }
}

private typealias NativeHook = (Any) -> Any?

internal data class ConfigFragment(
    val filename: String,
    val launchOptions: Json?,
    val contextOptions: Json?,
    val afterLaunch: NativeHook?,
    val afterContext: NativeHook?,
    val afterPage: NativeHook?,
)

private val configurationLock = Mutex()
private var configuration: PlaywrightRunConfiguration? = null

internal suspend fun readPlaywrightRunConfiguration(
    directory: String?,
    profile: String,
    browser: String,
): PlaywrightRunConfiguration = configurationLock.withLock {
    configuration ?: loadConfiguration(directory, profile, browser).also { configuration = it }
}

private suspend fun loadConfiguration(directory: String?, profile: String, browser: String): PlaywrightRunConfiguration {
    if (directory == null || !ConfigFileSystem.existsSync(directory)) return PlaywrightRunConfiguration()
    val settings = json("profile" to profile, "browser" to browser)
    val filenames = ConfigFileSystem.readdirSync(directory)
        .filter { it.endsWith(".mjs") || it.endsWith(".cjs") || it.endsWith(".js") }
        .sorted()
    val fragments = filenames.map { filename ->
        try {
            val url = ConfigUrl.pathToFileURL(ConfigPath.join(directory, filename)).href
            val exported = importFragment(url).await().asDynamic().default
            val fragment = if (jsTypeOf(exported) == "function") {
                val factory = exported.unsafeCast<(Json) -> Any?>()
                Promise.resolve(factory(settings)).await()
            } else {
                exported
            }
            check(fragment != null && jsTypeOf(fragment) == "object") { "Expected a default configuration object or factory" }
            fragment.unsafeCast<NativeConfig>().let {
                ConfigFragment(filename, it.launchOptions, it.contextOptions, it.afterLaunch, it.afterContext, it.afterPage)
            }
        } catch (error: Throwable) {
            throw IllegalStateException("Could not load Playwright configuration fragment $filename", error)
        }
    }
    return PlaywrightRunConfiguration(fragments)
}

private external interface NativeConfig {
    val launchOptions: Json?
    val contextOptions: Json?
    val afterLaunch: NativeHook?
    val afterContext: NativeHook?
    val afterPage: NativeHook?
}

private val importFragment: (String) -> Promise<Any> = js("new Function('url', 'return import(url)')")

@JsModule("node:fs")
private external object ConfigFileSystem {
    fun existsSync(path: String): Boolean
    fun readdirSync(path: String): Array<String>
}

@JsModule("node:path")
private external object ConfigPath {
    fun join(directory: String, filename: String): String
}

@JsModule("node:url")
private external object ConfigUrl {
    fun pathToFileURL(path: String): FileUrl
}

private external interface FileUrl {
    val href: String
}
