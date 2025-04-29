package com.zegreatrob.wrapper.wdio

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KCallable
import kotlin.time.measureTimedValue

private val webdriverBrowserLogger by lazy { KotlinLogging.logger("wdio-logger") }

interface BrowserLoggingSyntax {
    val logger get() = webdriverBrowserLogger

    suspend fun <T> log(workType: KCallable<*>, browserWork: suspend () -> T) = log(workType.name, browserWork)

    suspend fun <T> log(workType: String, browserWork: suspend () -> T): T {
        val measureTimeWithResult = measureTimedValue { browserWork() }
        logger.info {
            mapOf("workType" to workType, "duration" to "${measureTimeWithResult.duration}")
        }
        return measureTimeWithResult.value
    }
}
