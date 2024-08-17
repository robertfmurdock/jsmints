@file:JsModule("@wdio/cli")

package external.wdio.cli

import kotlin.js.Json
import kotlin.js.Promise

external class Launcher(configPath: String, options: Json) {
    fun run(): Promise<Int>
}
