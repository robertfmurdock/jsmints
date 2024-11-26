package com.zegreatrob.react.dataloader

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.ReactNode
import react.StateSetter
import react.useState

typealias DataLoadFunc<D> = suspend (DataLoaderTools) -> D

external interface DataLoaderProps<D> : Props {
    var getDataAsync: DataLoadFunc<D>
    var errorData: (Throwable) -> D
    var scope: CoroutineScope?
    var child: (value: DataLoadState<D>) -> ReactNode
}

@ReactFunc
val DataLoader: FC<DataLoaderProps<Any?>> by nfc { props ->
    val (getDataAsync, errorData, injectedScope, child) = props
    val (state, setState) = useState<DataLoadState<Any?>> { EmptyState() }
    val scope = injectedScope ?: useScope("Data load")

    if (state is EmptyState) {
        startPendingJob(scope, setState, getDataAsync, errorData)
    }

    +child(state)
}

private fun <D> startPendingJob(
    scope: CoroutineScope,
    setState: StateSetter<DataLoadState<D>>,
    getDataAsync: DataLoadFunc<D>,
    errorData: (Throwable) -> D,
) {
    val setEmpty = setState.empty()
    val setPending = setState.pending()
    val setResolved = setState.resolved()
    val tools = DataLoaderTools(scope, setEmpty)
    setPending(
        scope.launch { getDataOrCatchError(getDataAsync, tools, setResolved, errorData) }
            .also { job -> job.errorOnTotalJobFailure(setResolved, errorData) },
    )
}

private suspend fun <D> getDataOrCatchError(
    getDataAsync: DataLoadFunc<D>,
    tools: DataLoaderTools,
    setResolved: (D) -> Unit,
    errorData: (Throwable) -> D,
) {
    try {
        getDataAsync(tools).let(setResolved)
    } catch (cause: Throwable) {
        setResolved(errorData(cause))
    }
}

private fun <D> Job.errorOnTotalJobFailure(setResolved: (D) -> Unit, errorResult: (Throwable) -> D) = invokeOnCompletion { cause ->
    if (cause != null) {
        setResolved(errorResult(cause))
    }
}

private fun <D> StateSetter<DataLoadState<D>>.empty(): () -> Unit = {
    this(
        EmptyState(),
    )
}

private fun <D> StateSetter<DataLoadState<D>>.pending(): (Job) -> Unit = {
    this(
        PendingState(),
    )
}

private fun <D> StateSetter<DataLoadState<D>>.resolved(): (D) -> Unit = {
    this(
        ResolvedState(it),
    )
}
