package com.zegreatrob.wrapper.wdio.cli

import kotlinx.coroutines.await
import kotlin.js.json

suspend fun runWebdriverIO(configPath: String) = Launcher(configPath, json())
    .run()
    .await()
