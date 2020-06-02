package com.zegreatrob.testmints.action.async

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.testmints.action.Action
import com.zegreatrob.testmints.action.ExecutableActionExecuteSyntax
import com.zegreatrob.testmints.action.SimpleExecutableAction
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.coroutineScope
import kotlin.test.Test

class SuspendActionTest : SuspendActionExecuteSyntax {

    data class MultiplyAction(val left: Int, val right: Int) : SimpleSuspendAction<MultiplyActionDispatcher, Int> {
        override val performFunc = link(MultiplyActionDispatcher::invoke)
    }

    interface MultiplyActionDispatcher {
        suspend fun invoke(action: MultiplyAction) = coroutineScope {
            (1..action.left)
                .fold(0) { current, _ -> current + action.right }
        }
    }

    @Test
    fun usingTheActionWithTheDispatcherDoesTheWorkOfTheDispatchFunction() = asyncSetup(object {
        val action = MultiplyAction(2, 3)
        val dispatcher = object : MultiplyActionDispatcher {}
    }) exercise {
        dispatcher.execute(action)
    } verify { result ->
        result.assertIsEqualTo(6)
    }

    @Test
    fun executingActionMerelyPassesActionToDispatcherWhereWorkCanBeDone() = asyncSetup(object {
        val action = MultiplyAction(2, 3)
        val expectedReturn = 42
        val spy = SpyData<MultiplyAction, Int>().apply { spyWillReturn(expectedReturn) }
        val spyDispatcher = object : MultiplyActionDispatcher {
            override suspend fun invoke(action: MultiplyAction) = spy.spyFunction(action)
        }
    }) exercise {
        spyDispatcher.execute(action)
    } verify { result ->
        result.assertIsEqualTo(expectedReturn)
        spy.spyReceivedValues.assertIsEqualTo(listOf(action))
    }

    class Dispatcher : SuspendActionExecuteSyntax, ExecutableActionExecuteSyntax {

        data class AddAction(val left: Int, val right: Int) : SimpleSuspendAction<AddActionDispatcher, Int> {
            override val performFunc = link(AddActionDispatcher::invoke)
        }

        interface AddActionDispatcher {
            suspend operator fun invoke(addAction: AddAction) = with(addAction) { left + right }
        }

        @Test
        fun singleDispatcherObjectCanExecuteManyActions() = asyncSetup(object {
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

        data class SubtractAction(val left: Int, val right: Int) :
                SimpleExecutableAction<SubtractActionDispatcher, Int> {
            override val performFunc = link(SubtractActionDispatcher::invoke)
        }

        interface SubtractActionDispatcher {
            operator fun invoke(SubtractAction: SubtractAction) = with(SubtractAction) { left - right }
        }

        @Test
        fun singleDispatcherObjectCanWorkWithBothExecuteAndSuspendActions() = asyncSetup(object {
            val dispatcher = object : AddActionDispatcher, SubtractActionDispatcher {}
            val addAction = AddAction(7, 22)
            val subtractAction = SubtractAction(41, 13)
        }) exercise {
            Pair(
                dispatcher.execute(addAction),
                dispatcher.execute(subtractAction)
            )
        } verify { result ->
            result.assertIsEqualTo(
                29 to 28
            )
        }

        @Test
        fun usingSuspendActionSyntaxAllowsInterceptionOfAnyAction() = asyncSetup(object : SuspendActionExecuteSyntax {
            val dispatcher = object : AddActionDispatcher, MultiplyActionDispatcher {}
            val addAction = AddAction(7, 22)
            val multiplyAction = MultiplyAction(13, 41)

            val allExecutedActions = mutableListOf<Action>()
            override suspend fun <D, R> D.execute(action: SuspendAction<D, R>) = action.execute(this)
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

}
