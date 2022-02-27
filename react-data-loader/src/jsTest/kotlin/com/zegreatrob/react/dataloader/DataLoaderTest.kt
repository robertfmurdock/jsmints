package com.zegreatrob.react.dataloader

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.invoke
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import react.ChildrenBuilder
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.useState
import kotlin.test.Test

@ExperimentalCoroutinesApi
class DataLoaderTest {

    @Test
    fun willStartDataPullAndTransitionThroughNormalStatesCorrectly() = asyncSetup(object : ScopeMint() {
        val allRenderedStates = mutableListOf<DataLoadState<String>>()
    }) exercise {
        shallow(
            DataLoader(
                getDataAsync = { "DATA" },
                errorData = { "ERROR" },
                scope = exerciseScope
            ) { state ->
                allRenderedStates.add(state)
                div { +"state: $state" }
            }
        )
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
    }) exercise {
        shallow(
            DataLoader(getDataAsync, { "ERROR" }, exerciseScope) { state ->
                allRenderedStates.add(state)
                div { +"state: $state" }
            }
        )
    } verify {
        allRenderedStates.assertIsEqualTo(
            listOf(EmptyState(), PendingState(), ResolvedState("ERROR"))
        )
    }

    @Test
    fun usingTheReloadFunctionWillRunStatesAgain() = asyncSetup(object : ScopeMint() {
        val allRenderedStates = mutableListOf<DataLoadState<DataLoaderTools?>>()
        val wrapper = shallow(
            DataLoader({ it }, { null }, exerciseScope) { state ->
                allRenderedStates.add(state)
                div {
                    whenResolvedSuccessfully(state) { tools ->
                        button { this.onClick = { tools.reloadData() } }
                    }
                }
            }
        )
    }) exercise {
        wrapper.find<Props>("button").simulate("click")
    } verify {
        allRenderedStates.map { it::class }.assertIsEqualTo(
            listOf(EmptyState::class, PendingState::class, ResolvedState::class) +
                listOf(EmptyState::class, PendingState::class, ResolvedState::class)
        )
    }

    @Test
    fun childrenCanPerformAsyncWorkUsingDataLoaderScopeViaDataLoadTools() = asyncSetup(object : ScopeMint() {
        val channel = Channel<Int>()

        val wrapper = shallow(
            DataLoader({ tools -> tools }, { null }, exerciseScope) { state ->
                div {
                    whenResolvedSuccessfully(state) { tools -> buttonWithAsyncAction(tools) }
                }
            }
        )

        suspend fun collectThreeValuesFromChannel(): List<Int> {
            val e1 = channel.receive()
            val e2 = channel.receive()
            val e3 = channel.receive()
            return listOf(e1, e2, e3)
        }

        private fun ChildrenBuilder.buttonWithAsyncAction(tools: DataLoaderTools) {
            val (buttonClickValues, setValues) = useState<List<Int>?>(null)
            val onClick = { tools.performAsyncWork(::collectThreeValuesFromChannel, { throw it }, { setValues(it) }) }
            button { this.onClick = { onClick() } }
            div {
                className = "work-complete-div"
                if (buttonClickValues != null)
                    +"Work Complete ${buttonClickValues.joinToString(separator = ", ")}"
            }
        }
    }) exercise {
        wrapper.find<Props>("button").simulate("click")
        channel.send(99)
        channel.send(87)
        channel.send(53)

        channel.close()
    } verify {
        wrapper.find<Props>(".work-complete-div")
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
