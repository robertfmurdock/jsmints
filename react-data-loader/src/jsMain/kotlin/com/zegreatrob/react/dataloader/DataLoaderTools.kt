package com.zegreatrob.react.dataloader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class DataLoaderTools(val scope: CoroutineScope, val reloadData: ReloadFunc) {

    fun <R> performAsyncWork(
        work: suspend () -> R,
        errorResult: (Throwable) -> R,
        onWorkComplete: (R) -> Unit
    ) = scope.launch { work().let(onWorkComplete) }
        .handleOnCompletion(errorResult, onWorkComplete)

    private fun <R> Job.handleOnCompletion(
        errorResult: (Throwable) -> R,
        onWorkComplete: (R) -> Unit
    ) = invokeOnCompletion { throwable ->
        if (throwable != null) {
            errorResult(throwable).let(onWorkComplete)
        }
    }

}
