package com.zegreatrob.testmints.action

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.setup
import com.zegreatrob.testmints.invoke
import kotlin.test.Test

private typealias DivideActionDispatcher = (GeneralExecutableActionDispatcherTest.DivideAction) -> Int

class GeneralExecutableActionDispatcherTest {

    data class DivideAction(val left: Int, val right: Int) : SimpleExecutableAction<DivideActionDispatcher, Int> {
        override val performFunc = link(DivideActionDispatcher::invoke)
    }

    @Test
    fun syntaxAllowsInterceptionOfActionExecutionIncludingReplacingResult() = setup(object :
        GeneralExecutableActionDispatcherSyntax {
        val expectedReplacedResult = 127
        override val generalDispatcher = generalDispatcherSpy().apply { spyWillReturn(expectedReplacedResult) }
        val action = DivideAction(6, 7)
        val divideDispatcherSpy = SpyData<DivideAction, Int>()
        val divideDispatcher = divideDispatcherSpy::spyFunction
    }) exercise {
        divideDispatcher.execute(action)
    } verify { result ->
        result.assertIsEqualTo(expectedReplacedResult)
        generalDispatcher.spyReceivedValues
            .assertIsEqualTo(listOf(action to divideDispatcher))
        divideDispatcherSpy.spyReceivedValues
            .assertIsEqualTo(emptyList<Any>())
    }

    private fun generalDispatcherSpy() = object : GeneralExecutableActionDispatcher,
        Spy<Pair<ExecutableAction<*, *>, *>, Any> by SpyData() {
        @Suppress("UNCHECKED_CAST")
        override fun <D, R> dispatch(action: ExecutableAction<D, R>, dispatcher: D): R =
            spyFunction(action to dispatcher) as R
    }

}
