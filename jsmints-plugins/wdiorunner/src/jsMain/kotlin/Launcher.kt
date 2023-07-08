import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.Promise

external interface Launcher {
    fun run(): Promise<Int>
}

suspend fun Launcher(configPath: String, options: Json): Launcher = launcher(getConstructor(), configPath, options)

@Suppress("UNUSED_PARAMETER")
private fun launcher(constructor: dynamic, configPath: String, options: Json): Launcher =
    js("new constructor(configPath, options)")
        .unsafeCast<Launcher>()

private suspend fun getConstructor(): dynamic = Promise.resolve(
    js("import(\"@wdio/cli\")").unsafeCast<Promise<Json>>(),
)
    .then { it["Launcher"].asDynamic() }
    .await()
