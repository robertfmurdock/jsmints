package com.zegreatrob.testmints.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.joinAll

internal suspend fun waitForJobsToFinish(scope: CoroutineScope) {
    val job = scope.coroutineContext[Job]
    job?.children?.toList()?.joinAll()
    scope.cancel()
}