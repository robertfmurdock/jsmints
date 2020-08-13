package com.zegreatrob.wrapper.wdio

import com.soywiz.klock.measureTimeWithResult
import mu.KotlinLogging
import kotlin.reflect.KCallable

private val webdriverBrowserLogger by lazy { KotlinLogging.logger("wdio-logger") }

interface BrowserLoggingSyntax {
    val logger get() = webdriverBrowserLogger

    suspend fun <T> log(workType: KCallable<*>, browserWork: suspend () -> T) = log(workType.name, browserWork)

    suspend fun <T> log(workType: String, browserWork: suspend () -> T): T {
        val measureTimeWithResult = measureTimeWithResult { browserWork() }
        logger.info {
            mapOf("workType" to workType, "duration" to "${measureTimeWithResult.time}")
        }
        return measureTimeWithResult.result
    }
}