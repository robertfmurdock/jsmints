package com.zegreatrob.testmints.action.async

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.invoke
import kotlin.test.Test

private typealias DivideActionDispatcher = suspend (GeneralSuspendActionDispatcherTest.DivideAction) -> Int

class GeneralSuspendActionDispatcherTest {

    data class DivideAction(val left: Int, val right: Int) : SimpleSuspendAction<DivideActionDispatcher, Int> {
        override val performFunc = link(DivideActionDispatcher::invoke)
    }

    @Test
    fun syntaxAllowsInterceptionOfActionExecutionIncludingReplacingResult() = asyncSetup(object :
        GeneralSuspendActionDispatcherSyntax {
        val expectedReplacedResult = 127
        override val generalDispatcher = generalDispatcherSpy().apply { spyWillReturn(expectedReplacedResult) }
        val action = DivideAction(6, 7)
        val divideDispatcherSpy = SpyData<DivideAction, Int>()
        val divideDispatcher: DivideActionDispatcher = { divideDispatcherSpy.spyFunction(it) }
    }) exercise {
        divideDispatcher.execute(action)
    } verify { result ->
        result.assertIsEqualTo(expectedReplacedResult)
        generalDispatcher.spyReceivedValues
            .assertIsEqualTo(listOf(action to divideDispatcher))
        divideDispatcherSpy.spyReceivedValues
            .assertIsEqualTo(emptyList<Any>())
    }

    private fun generalDispatcherSpy() = object : GeneralSuspendActionDispatcher,
        Spy<Pair<SuspendAction<*, *>, *>, Any> by SpyData() {
        @Suppress("UNCHECKED_CAST")
        override suspend fun <D, R> dispatch(action: SuspendAction<D, R>, dispatcher: D): R =
            spyFunction(action to dispatcher) as R
    }

}
