package com.zegreatrob.testmints

inline fun captureException(work: () -> Unit) = try {
    work()
    null
} catch (expectedFailure: Throwable) {
    expectedFailure
}