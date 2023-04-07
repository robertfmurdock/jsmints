import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

fun main() {
    val config = wdioConfig()

    MainScope().launch {
        runWebdriverIO(config, fgrepString())
            .let { result -> process.exit(result) }
    }.invokeOnCompletion { huh ->
        if (huh != null) {
            process.exit(reportError(huh))
        }
    }
}

private fun fgrepString() = process.envString("FGREP")

private fun Process.envString(key: String) = env[key].unsafeCast<String>()

private fun wdioConfig() = process.envString("WDIO_CONFIG")

private fun reportError(error: Throwable) = (-1).also { console.log("Error", error) }
