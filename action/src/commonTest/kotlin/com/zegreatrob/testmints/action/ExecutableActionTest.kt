package com.zegreatrob.testmints.action

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.setup
import com.zegreatrob.testmints.invoke
import kotlin.test.Test

class ExecutableActionTest : ExecutableActionExecuteSyntax {

    data class MultiplyAction(val left: Int, val right: Int) : SimpleExecutableAction<MultiplyActionDispatcher, Int> {
        override val performFunc = link(MultiplyActionDispatcher::invoke)
    }

    interface MultiplyActionDispatcher {
        fun invoke(action: MultiplyAction) = action.run {
            (1..left)
                .fold(0) { current, _ -> current + right }
        }
    }

    @Test
    fun usingTheActionWithTheDispatcherDoesTheWorkOfTheDispatchFunction() = setup(object {
        val action = MultiplyAction(2, 3)
        val dispatcher: MultiplyActionDispatcher = object : MultiplyActionDispatcher {}
    }) exercise {
        dispatcher.execute(action)
    } verify { result ->
        result.assertIsEqualTo(6)
    }

    @Test
    fun executingActionMerelyPassesActionToDispatcherWhereWorkCanBeDone() = setup(object {
        val action = MultiplyAction(2, 3)
        val expectedReturn = 42
        val spy = SpyData<MultiplyAction, Int>().apply { spyWillReturn(expectedReturn) }
        val spyDispatcher = object : MultiplyActionDispatcher {
            override fun invoke(action: MultiplyAction) = spy.spyFunction(action)
        }
    }) exercise {
        spyDispatcher.execute(action)
    } verify { result ->
        result.assertIsEqualTo(expectedReturn)
        spy.spyReceivedValues.assertIsEqualTo(listOf(action))
    }

    data class AddAction(val left: Int, val right: Int) : SimpleExecutableAction<AddActionDispatcher, Int> {
        override val performFunc = link(AddActionDispatcher::invoke)
    }

    interface AddActionDispatcher {
        operator fun invoke(addAction: AddAction) = with(addAction) { left + right }
    }

    @Test
    fun singleDispatcherObjectCanExecuteManyActions() = setup(object {
        val dispatcher = object : AddActionDispatcher, MultiplyActionDispatcher {}
        val addAction = AddAction(7, 22)
        val multiplyAction = MultiplyAction(13, 41)
    }) exercise {
        Pair(
            dispatcher.execute(addAction),
            dispatcher.execute(multiplyAction)
        )
    } verify { result ->
        with(result) {
            first.assertIsEqualTo(29)
            second.assertIsEqualTo(533)
        }
    }

    @Test
    fun usingExecutableActionSyntaxAllowsInterceptionOfAnyAction() = setup(object : ExecutableActionExecuteSyntax {
        val dispatcher = object : AddActionDispatcher, MultiplyActionDispatcher {}
        val addAction = AddAction(7, 22)
        val multiplyAction = MultiplyAction(13, 41)

        val allExecutedActions = mutableListOf<Action>()
        override fun <D, R> D.execute(action: ExecutableAction<D, R>) = action.execute(this)
            .also { allExecutedActions.add(action) }
    }) exercise {
        Pair(
            dispatcher.execute(addAction),
            dispatcher.execute(multiplyAction)
        )
    } verify {
        allExecutedActions.assertIsEqualTo(
            listOf(addAction, multiplyAction)
        )
    }
}
