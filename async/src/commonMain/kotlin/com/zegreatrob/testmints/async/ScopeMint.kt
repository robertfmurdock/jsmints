package com.zegreatrob.testmints.async

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.plus

abstract class ScopeMint {
    val testScope = mintScope()
    val setupScope = mintScope() + CoroutineName("Setup")
    val exerciseScope = mintScope() + CoroutineName("Exercise")
}
