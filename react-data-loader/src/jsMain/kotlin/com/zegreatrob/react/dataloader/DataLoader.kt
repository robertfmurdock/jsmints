package com.zegreatrob.react.dataloader

import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.tmFC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import react.ChildrenBuilder
import react.StateSetter
import react.useState

typealias DataLoadFunc<D> = suspend (DataLoaderTools) -> D

@Deprecated(
    replaceWith = ReplaceWith("DataLoaderProps"),
    level = DeprecationLevel.WARNING,
    message = "Name to be removed."
)
typealias DataLoadWrapperProps<D> = DataLoaderProps<D>

data class DataLoaderProps<D>(
    val getDataAsync: DataLoadFunc<D>,
    val errorData: (Throwable) -> D,
    val scope: CoroutineScope? = null,
    val children: ChildrenBuilder.(value: DataLoadState<D>) -> Unit
) : DataProps

private val cachedComponent = tmFC<DataLoaderProps<Any>> { props ->
    val (getDataAsync, errorData, injectedScope) = props
    val (state, setState) = useState<DataLoadState<Any>> { EmptyState() }
    val scope = injectedScope ?: useScope("Data load")

    if (state is EmptyState) {
        startPendingJob(scope, setState, getDataAsync, errorData)
    }

    props.children(this, state)
}

fun <D> dataLoader() = cachedComponent.unsafeCast<TMFC<DataLoaderProps<D>>>()

private fun <D> startPendingJob(
    scope: CoroutineScope,
    setState: StateSetter<DataLoadState<D>>,
    getDataAsync: DataLoadFunc<D>,
    errorData: (Throwable) -> D
) {
    val setEmpty = setState.empty()
    val setPending = setState.pending()
    val setResolved = setState.resolved()
    val tools = DataLoaderTools(scope, setEmpty)
    setPending(
        scope.launch { getDataAsync(tools).let(setResolved) }
            .also { job -> job.errorOnJobFailure(setResolved, errorData) }
    )
}

private fun <D> Job.errorOnJobFailure(setResolved: (D) -> Unit, errorResult: (Throwable) -> D) =
    invokeOnCompletion { cause -> if (cause != null) setResolved(errorResult(cause)) }

private fun <D> StateSetter<DataLoadState<D>>.empty(): () -> Unit = {
    this(
        EmptyState()
    )
}

private fun <D> StateSetter<DataLoadState<D>>.pending(): (Job) -> Unit = {
    this(
        PendingState()
    )
}

private fun <D> StateSetter<DataLoadState<D>>.resolved(): (D) -> Unit = {
    this(
        ResolvedState(it)
    )
}
