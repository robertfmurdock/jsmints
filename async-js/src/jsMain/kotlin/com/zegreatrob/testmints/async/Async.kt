package com.zegreatrob.testmints.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

actual fun <T> testAsync(block: suspend CoroutineScope.() -> T): Any? = GlobalScope.promise(block = block)