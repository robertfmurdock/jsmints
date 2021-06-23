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
import react.dom.attrs
import react.dom.button
import react.dom.div
import react.useState
import kotlin.test.Test

@ExperimentalCoroutinesApi
class DataLoaderTest {

    @Test
    fun willStartDataPullAndTransitionThroughNormalStatesCorrectly() = asyncSetup(object : ScopeMint() {
        val allRenderedStates = mutableListOf<DataLoadState<String>>()

        fun RBuilder.component() = dataLoader(
            getDataAsync = { "DATA" },
            errorData = { "ERROR" },
            scope = exerciseScope
        ) { state ->
            allRenderedStates.add(state)
            div { +"state: $state" }
        }
    }) exercise {
        shallow { component() }
    } verify {
        allRenderedStates.assertIsEqualTo(
            listOf(EmptyState(), PendingState(), ResolvedState("DATA"))
        )
    }

    @Test
    fun whenDataPullIsCancelledErrorDataIsPushedToChild() = asyncSetup(object : ScopeMint() {
        val allRenderedStates = mutableListOf<DataLoadState<String>>()

        val getDataAsync: suspend (DataLoaderTools) -> Nothing = {
            withContext(exerciseScope.coroutineContext) { throw Exception("NOPE") }
        }

        fun RBuilder.component() = dataLoader(getDataAsync, { "ERROR" }, exerciseScope) { state ->
            allRenderedStates.add(state)
            div { +"state: $state" }
        }
    }) exercise {
        shallow { component() }
    } verify {
        allRenderedStates.assertIsEqualTo(
            listOf(EmptyState(), PendingState(), ResolvedState("ERROR"))
        )
    }

    @Test
    fun usingTheReloadFunctionWillRunStatesAgain() = asyncSetup(object : ScopeMint() {
        val allRenderedStates = mutableListOf<DataLoadState<DataLoaderTools?>>()

        fun RBuilder.component() = dataLoader({ it }, { null }, exerciseScope) { state ->
            allRenderedStates.add(state)
            div {
                whenResolvedSuccessfully(state) { tools ->
                    button { attrs { onClickFunction = { tools.reloadData() } } }
                }
            }
        }

        val wrapper = shallow { component() }
    }) exercise {
        wrapper.find<RProps>("button").simulate("click")
    } verify {
        allRenderedStates.map { it::class }.assertIsEqualTo(
            listOf(EmptyState::class, PendingState::class, ResolvedState::class)
                    + listOf(EmptyState::class, PendingState::class, ResolvedState::class)
        )
    }


    @Test
    fun childrenCanPerformAsyncWorkUsingDataLoaderScopeViaDataLoadTools() = asyncSetup(object : ScopeMint() {
        val channel = Channel<Int>()

        val wrapper = shallow {
            dataLoader({ tools -> tools }, { null }, exerciseScope) { state ->
                div {
                    whenResolvedSuccessfully(state) { tools -> buttonWithAsyncAction(tools) }
                }
            }
        }

        suspend fun collectThreeValuesFromChannel(): List<Int> {
            val e1 = channel.receive()
            val e2 = channel.receive()
            val e3 = channel.receive()
            return listOf(e1, e2, e3)
        }

        private fun RBuilder.buttonWithAsyncAction(tools: DataLoaderTools) {
            val (buttonClickValues, setValues) = useState<List<Int>?>(null)
            val onClick = { tools.performAsyncWork(::collectThreeValuesFromChannel, { throw it }, { setValues(it) }) }

            button { attrs { onClickFunction = { onClick() } } }
            div(classes = "work-complete-div") {
                if (buttonClickValues != null)
                    +"Work Complete ${buttonClickValues.joinToString(separator = ", ")}"
            }
        }
    }) exercise {
        wrapper.find<RProps>("button").simulate("click")
        channel.send(99)
        channel.send(87)
        channel.send(53)

        channel.close()
    } verify {
        wrapper.find<RProps>(".work-complete-div")
            .text()
            .assertIsEqualTo("Work Complete 99, 87, 53")
    }

    companion object {
        private fun <D> whenResolvedSuccessfully(state: DataLoadState<D?>, handler: (D) -> Unit) {
            if (state is ResolvedState) {
                state.result?.let(handler)
            }
        }
    }
}