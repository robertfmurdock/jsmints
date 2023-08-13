package com.zegreatrob.react.dataloader

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.react.dataloader.external.testinglibrary.userevent.userEvent
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.waitFor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import react.FC
import react.Fragment
import react.Props
import react.create
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
            Fragment.create {
                DataLoader(
                    getDataAsync = { "DATA" },
                    errorData = { "ERROR" },
                    scope = exerciseScope,
                    child = { state ->
                        allRenderedStates.add(state)
                        div.create { +"state: $state" }
                    },
                )
            },
        )
    } verify {
        screen.findByText("state: ${ResolvedState("DATA")}")
        allRenderedStates.assertIsEqualTo(
            listOf(EmptyState(), PendingState(), ResolvedState("DATA")),
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
            Fragment.create {
                DataLoader(
                    getDataAsync = getDataAsync,
                    errorData = { "ERROR" },
                    scope = exerciseScope,
                    child = { state ->
                        allRenderedStates.add(state)
                        div.create { +"state: $state" }
                    },
                )
            },
        )
    } verify {
        screen.findByText("state: ${ResolvedState("ERROR")}")
        allRenderedStates.assertIsEqualTo(
            listOf(EmptyState(), PendingState(), ResolvedState("ERROR")),
        )
    }

    @Test
    fun usingTheReloadFunctionWillRunStatesAgain() = asyncSetup(object : ScopeMint() {
        val allRenderedStates = mutableListOf<DataLoadState<DataLoaderTools?>>()
    }) {
        render(
            Fragment.create {
                DataLoader({ it }, { null }, exerciseScope, child = { state ->
                    allRenderedStates.add(state)
                    div.create {
                        div { +"allStatesCount: ${allRenderedStates.size}" }
                        whenResolvedSuccessfully(state) { tools ->
                            button {
                                +"Button"
                                this.onClick = { tools.reloadData() }
                            }
                        }
                    }
                })
            },
        )
    } exercise {
        userEvent.click(screen.findByText("Button"))
    } verify {
        screen.findByText("allStatesCount: 6")

        allRenderedStates.map { it::class }.assertIsEqualTo(
            listOf(EmptyState::class, PendingState::class, ResolvedState::class) +
                listOf(EmptyState::class, PendingState::class, ResolvedState::class),
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
            button {
                +"Button"
                this.onClick = { onClick() }
            }
            div {
                if (buttonClickValues != null) {
                    +"Work Complete ${buttonClickValues.joinToString(separator = ", ")}"
                }
            }
        }
    }) {
        render(
            Fragment.create {
                DataLoader({ tools -> tools }, { null }, exerciseScope, child = { state ->
                    div.create {
                        whenResolvedSuccessfully(state) { tools ->
                            buttonWithAsyncAction {
                                asDynamic()["tools"] = tools
                            }
                        }
                    }
                })
            },
        )
    } exercise {
        userEvent.click(screen.findByText("Button"))

        channel.send(99)
        channel.send(87)
        channel.send(53)

        channel.close()
    } verify {
        waitFor {
            screen.getByText("Work Complete 99, 87, 53")
                .assertIsNotEqualTo(null)
        }
    }

    companion object {
        private fun <D> whenResolvedSuccessfully(state: DataLoadState<D?>, handler: (D) -> Unit) {
            if (state is ResolvedState) {
                state.result?.let(handler)
            }
        }
    }
}
