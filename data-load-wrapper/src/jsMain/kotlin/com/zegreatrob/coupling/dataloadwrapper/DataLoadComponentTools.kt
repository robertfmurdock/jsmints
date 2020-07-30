package com.zegreatrob.coupling.dataloadwrapper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async

typealias ReloadFunction = () -> Unit

data class DataLoadComponentTools(val scope: CoroutineScope, val reloadData: ReloadFunction) {

    @ExperimentalCoroutinesApi
    fun <R> performAsyncWork(
        work: suspend () -> R,
        errorResult: (Throwable) -> R,
        onWorkComplete: (R) -> Unit
    ) = scope.async { work() }
        .handleOnCompletion(onWorkComplete, errorResult)

    @ExperimentalCoroutinesApi
    private fun <R> Deferred<R>.handleOnCompletion(
        onWorkComplete: (R) -> Unit,
        errorResult: (Throwable) -> R
    ) = invokeOnCompletion { throwable ->
        if (throwable == null)
            getCompleted().let(onWorkComplete)
        else
            errorResult(throwable).let(onWorkComplete)
    }

}
