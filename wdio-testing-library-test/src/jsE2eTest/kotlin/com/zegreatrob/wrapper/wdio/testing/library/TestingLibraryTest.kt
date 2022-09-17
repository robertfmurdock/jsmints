package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class TestingLibraryTest {

    @Test
    fun canUseQueriesToFindElements() = asyncSetup(object : ScopeMint() {
    }) exercise {
    } verify {
    }
}
