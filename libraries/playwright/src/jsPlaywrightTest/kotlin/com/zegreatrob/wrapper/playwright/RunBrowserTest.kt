package com.zegreatrob.wrapper.playwright

import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RunBrowserTest {
    private val browserSetup = asyncTestTemplate(
        sharedSetup = { RunBrowser() },
        sharedTeardown = { owner: RunBrowser -> owner.close() },
    )

    @Test
    fun repeatedAcquisitionUsesTheConnectedBrowser() = browserSetup() exercise {
        val first = acquire { configuredBrowserType().launch() }
        first to acquire { error("A connected browser should be reused") }
    } verify { (first, second) ->
        assertSame(first, second)
    }

    @Test
    fun acquisitionReplacesADisconnectedBrowser() = browserSetup.with({ owner ->
        val disconnected = owner.acquire { configuredBrowserType().launch() }
        disconnected.close()
        owner to disconnected
    }) exercise {
        first.acquire { configuredBrowserType().launch() }
    } verify { replacement ->
        assertNotSame(second, replacement)
        assertTrue(replacement.isConnected)
    }

    @Test
    fun closingTheOwnerDisconnectsTheBrowser() = browserSetup.with({ owner ->
        owner to owner.acquire { configuredBrowserType().launch() }
    }) exercise {
        first.close()
    } verify {
        assertFalse(second.isConnected)
    }
}
