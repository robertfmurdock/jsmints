import kotlinx.coroutines.await
import kotlin.js.json

suspend fun runWebdriverIO(configPath: String, fgrepString: String) = Launcher(
    configPath = configPath,
    options = json("mochaOpts" to json("fgrep" to fgrepString)),
)
    .run()
    .await()
