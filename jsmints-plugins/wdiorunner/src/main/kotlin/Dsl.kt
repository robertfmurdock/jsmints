import kotlinx.coroutines.await
import kotlin.js.json

suspend fun runWebdriverIO(configPath: String) = Launcher(configPath, json())
    .run()
    .await()
