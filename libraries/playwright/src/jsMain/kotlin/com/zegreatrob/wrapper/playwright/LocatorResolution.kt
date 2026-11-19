package com.zegreatrob.wrapper.playwright

import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/** Resolves the current match count and requires either zero or one match. */
suspend fun Locator.queryOne(description: String): Locator? {
    val count = count()
    check(count <= 1) { "Expected at most one match for $description; found $count" }
    return if (count == 1) this else null
}

suspend fun Locator.requireOne(description: String): Locator = checkNotNull(queryOne(description)) {
    "No matches for $description"
}

/** Waits for exactly one match, including when duplicates disappear during the wait. */
suspend fun Locator.findOne(description: String, timeoutMillis: Int): Locator {
    require(timeoutMillis > 0) { "Timeout must be positive" }
    var matches = count()
    val found = withTimeoutOrNull(timeoutMillis.toLong()) {
        while (matches != 1) {
            delay(20)
            matches = count()
        }
        this@findOne
    }
    return checkNotNull(found) {
        "Expected one match for $description after ${timeoutMillis}ms; found $matches"
    }
}

suspend fun Locator.requireAll(description: String): List<Locator> = all().also {
    check(it.isNotEmpty()) { "No matches for $description" }
}

suspend fun Locator.findAll(timeoutMillis: Int): List<Locator> {
    first().waitForAttached(timeoutMillis)
    return all()
}
