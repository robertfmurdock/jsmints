package com.zegreatrob.testmints.async

import kotlinx.coroutines.*

@DelicateCoroutinesApi
internal fun mintScope() = GlobalScope + SupervisorJob() + CoroutineName("testMintAsync")
