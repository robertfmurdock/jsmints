package com.zegreatrob.coupling

import com.zegreatrob.coupling.dataloadwrapper.*
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.invoke
import kotlinx.coroutines.withContext
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.dom.button
import react.dom.div
import kotlin.test.Test

class DataLoadWrapperTest {

    @Test
    fun willStartDataPullAndTransitionThroughNormalStatesCorrectly() = asyncSetup(object : ScopeMint() {
        val component = dataLoadWrapper<String>()
        val allRenderedStates = mutableListOf<DataLoadState<String>>()
    }) exercise {
        shallow(
            component,
            DataLoadWrapperProps({ "DATA" }, { "ERROR" }, exerciseScope), { state: DataLoadState<String> ->
                allRenderedStates.add(state)
                div { +"state: $state" }
            })
    } verify {
        allRenderedStates.assertIsEqualTo(
            listOf(EmptyState(), PendingState(), ResolvedState("DATA"))
        )
    }

    @Test
    fun whenDataPullIsCancelledErrorDataIsPushedToChild() = asyncSetup(object : ScopeMint() {
        val component = dataLoadWrapper<String>()
        val allRenderedStates = mutableListOf<DataLoadState<String>>()

        val getDataAsync: suspend (DataLoadComponentTools) -> Nothing = {
            withContext(exerciseScope.coroutineContext) { throw Exception("NOPE") }
        }
    }) exercise {
        shallow(
            component,
            DataLoadWrapperProps(getDataAsync, { "ERROR" }, exerciseScope),
            { state: DataLoadState<String> ->
                allRenderedStates.add(state)
                div { +"state: $state" }
            })
    } verify {
        allRenderedStates.assertIsEqualTo(
            listOf(EmptyState(), PendingState(), ResolvedState("ERROR"))
        )
    }

    @Test
    fun usingTheReloadFunctionWillRunStatesAgain() = asyncSetup(object : ScopeMint() {
        val allRenderedStates = mutableListOf<DataLoadState<Result<DataLoadComponentTools>>>()

        val enzymeWrapper = shallow(
            dataLoadWrapper(),
            DataLoadWrapperProps({ Result.success(it) }, { Result.failure(it) }, exerciseScope),
            { state: DataLoadState<Result<DataLoadComponentTools>> ->
                allRenderedStates.add(state)
                div { reloadButton(state) }
            })

        private fun RBuilder.reloadButton(state: DataLoadState<Result<DataLoadComponentTools>>) {
            if (state !is ResolvedState)
                return
            val reloadData = state.result.getOrNull()?.reloadData
            if (reloadData != null)
                button { attrs { onClickFunction = { reloadData() } } }
        }
    }) exercise {
        enzymeWrapper.find<RProps>("button").simulate("click")
    } verify {
        allRenderedStates.map { it::class }.assertIsEqualTo(
            listOf(EmptyState::class, PendingState::class, ResolvedState::class)
                    + listOf(EmptyState::class, PendingState::class, ResolvedState::class)
        )
    }


}