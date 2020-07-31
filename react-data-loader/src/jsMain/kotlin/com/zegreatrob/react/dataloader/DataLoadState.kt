package com.zegreatrob.react.dataloader

sealed class DataLoadState<D>

data class EmptyState<D>(private val empty: String = "") : DataLoadState<D>()

data class PendingState<D>(private val empty: String = "") : DataLoadState<D>()

data class ResolvedState<D>(val result: D) : DataLoadState<D>()
