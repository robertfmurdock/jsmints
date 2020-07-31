package com.zegreatrob.react.dataloader

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.invoke
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.dom.button
import react.dom.div
import react.useState
import kotlin.test.Test

class DataLoaderTest {

    @Test
    fun willStartDataPullAndTransitionThroughNormalStatesCorrectly() = asyncSetup(object : ScopeMint() {
        val component = dataLoader<String>()
        val allRenderedStates = mutableListOf<DataLoadState<String>>()
    }) exercise {
        shallow(
            component,
            DataLoadWrapperProps(
                { "DATA" },
                { "ERROR" },
                exerciseScope
            ), { state: DataLoadState<String> ->
                allRenderedStates.add(state)
                div { +"state: $state" }
            })
    } verify {
        allRenderedStates.assertIsEqualTo(
            listOf(
                EmptyState(),
                PendingState(),
                ResolvedState("DATA")
            )
        )
    }

    @Test
    fun whenDataPullIsCancelledErrorDataIsPushedToChild() = asyncSetup(object : ScopeMint() {
        val component = dataLoader<String>()
        val allRenderedStates = mutableListOf<DataLoadState<String>>()

        val getDataAsync: suspend (DataLoaderTools) -> Nothing = {
            withContext(exerciseScope.coroutineContext) { throw Exception("NOPE") }
        }
    }) exercise {
        shallow(
            component,
            DataLoadWrapperProps(
                getDataAsync,
                { "ERROR" },
                exerciseScope
            ),
            { state: DataLoadState<String> ->
                allRenderedStates.add(state)
                div { +"state: $state" }
            })
    } verify {
        allRenderedStates.assertIsEqualTo(
            listOf(
                EmptyState(),
                PendingState(),
                ResolvedState("ERROR")
            )
        )
    }

    @Test
    fun usingTheReloadFunctionWillRunStatesAgain() = asyncSetup(object : ScopeMint() {
        val allRenderedStates = mutableListOf<DataLoadState<Result<DataLoaderTools>>>()

        val enzymeWrapper = shallow(
            dataLoader(),
            DataLoadWrapperProps(
                { Result.success(it) },
                { Result.failure(it) },
                exerciseScope
            ),
            { state: DataLoadState<Result<DataLoaderTools>> ->
                allRenderedStates.add(state)
                div { reloadButton(state) }
            })

        private fun RBuilder.reloadButton(state: DataLoadState<Result<DataLoaderTools>>) {
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


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun childrenCanUseScopeForSuspendableWork() = asyncSetup(object : ScopeMint() {
        val enzymeWrapper = shallow(
            dataLoader(),
            DataLoadWrapperProps({ Result.success(it) }, { Result.failure(it) }, exerciseScope),
            { state: DataLoadState<Result<DataLoaderTools>> ->
                div { whenResolvedSuccessfully(state) { buttonWithAsyncAction(it) } }
            })

        val channel = Channel<Int>()

        suspend fun collectThreeValuesFromChannel() = listOf(channel.receive(), channel.receive(), channel.receive())

        private fun whenResolvedSuccessfully(
            state: DataLoadState<Result<DataLoaderTools>>,
            handler: (DataLoaderTools) -> Unit
        ) {
            if (state is ResolvedState) {
                state.result.onSuccess(handler)
            }
        }

        private fun RBuilder.buttonWithAsyncAction(tools: DataLoaderTools) {
            val (buttonClickValues, setValues) = useState<List<Int>?>(null)
            val onClick = {
                tools.performAsyncWork(::collectThreeValuesFromChannel, { emptyList() }, { setValues(it) })
            }

            button { attrs { onClickFunction = { onClick() } } }
            div(classes = "work-complete-div") {
                if (buttonClickValues != null)
                    +"Work Complete ${buttonClickValues.joinToString(separator = ", ")}"
            }
        }
    }) exercise {
        enzymeWrapper.find<RProps>("button").simulate("click")
        channel.send(99)
        channel.send(87)
        channel.send(53)
        channel.close()
    } verify {
        enzymeWrapper.find<RProps>(".work-complete-div")
            .text()
            .assertIsEqualTo("Work Complete 99, 87, 53")
    }

}