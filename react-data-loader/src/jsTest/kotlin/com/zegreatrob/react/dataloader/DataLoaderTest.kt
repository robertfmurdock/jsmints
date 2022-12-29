package com.zegreatrob.react.dataloader

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.minreact.create
import com.zegreatrob.react.dataloader.external.testinglibrary.react.render
import com.zegreatrob.react.dataloader.external.testinglibrary.react.screen
import com.zegreatrob.react.dataloader.external.testinglibrary.react.waitFor
import com.zegreatrob.react.dataloader.external.testinglibrary.userevent.userEvent
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import react.FC
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
        render(
            DataLoader(
                getDataAsync = { "DATA" },
                errorData = { "ERROR" },
                scope = exerciseScope
            ) { state ->
                allRenderedStates.add(state)
                div { +"state: $state" }
            }.create()
        )
    } verify {
        screen.findByText("state: ${ResolvedState("DATA")}").await()
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
        render(
            DataLoader(getDataAsync, { "ERROR" }, exerciseScope) { state ->
                allRenderedStates.add(state)
                div { +"state: $state" }
            }.create()
        )
    } verify {
        screen.findByText("state: ${ResolvedState("ERROR")}").await()
        allRenderedStates.assertIsEqualTo(
            listOf(EmptyState(), PendingState(), ResolvedState("ERROR"))
        )
    }

    @Test
    fun usingTheReloadFunctionWillRunStatesAgain() = asyncSetup(object : ScopeMint() {
        val allRenderedStates = mutableListOf<DataLoadState<DataLoaderTools?>>()
    }) {
        render(
            DataLoader({ it }, { null }, exerciseScope) { state ->
                allRenderedStates.add(state)
                div {
                    div { +"allStatesCount: ${allRenderedStates.size}" }
                    whenResolvedSuccessfully(state) { tools ->
                        button { +"Button"; this.onClick = { tools.reloadData() } }
                    }
                }
            }.create()
        )
    } exercise {
        userEvent.click(screen.findByText("Button").await())
    } verify {
        screen.findByText("allStatesCount: 6").await()

        allRenderedStates.map { it::class }.assertIsEqualTo(
            listOf(EmptyState::class, PendingState::class, ResolvedState::class) +
                listOf(EmptyState::class, PendingState::class, ResolvedState::class)
        )
    }

    @Test
    fun childrenCanPerformAsyncWorkUsingDataLoaderScopeViaDataLoadTools() = asyncSetup(object : ScopeMint() {
        val channel = Channel<Int>()

        suspend fun collectThreeValuesFromChannel(): List<Int> {
            val e1 = channel.receive()
            val e2 = channel.receive()
            val e3 = channel.receive()
            return listOf(e1, e2, e3)
        }

        val buttonWithAsyncAction = FC<Props> { props ->
            val tools = props.asDynamic().tools.unsafeCast<DataLoaderTools>()
            val (buttonClickValues, setValues) = useState<List<Int>?>(null)
            val onClick = { tools.performAsyncWork(::collectThreeValuesFromChannel, { throw it }, { setValues(it) }) }
            button { +"Button"; this.onClick = { onClick() } }
            div {
                if (buttonClickValues != null) {
                    +"Work Complete ${buttonClickValues.joinToString(separator = ", ")}"
                }
            }
        }
    }) {
        render(
            DataLoader({ tools -> tools }, { null }, exerciseScope) { state ->
                div {
                    whenResolvedSuccessfully(state) { tools ->
                        buttonWithAsyncAction {
                            asDynamic()["tools"] = tools
                        }
                    }
                }
            }.create()
        )
    } exercise {
        userEvent.click(screen.findByText("Button").await())

        channel.send(99)
        channel.send(87)
        channel.send(53)

        channel.close()
    } verify {
        waitFor {
            screen.getByText("Work Complete 99, 87, 53")
                .assertIsNotEqualTo(null)
        }.await()
    }

    companion object {
        private fun <D> whenResolvedSuccessfully(state: DataLoadState<D?>, handler: (D) -> Unit) {
            if (state is ResolvedState) {
                state.result?.let(handler)
            }
        }
    }
}
