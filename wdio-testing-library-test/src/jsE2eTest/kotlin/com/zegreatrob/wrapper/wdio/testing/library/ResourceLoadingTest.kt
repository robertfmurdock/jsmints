package com.zegreatrob.wrapper.wdio.testing.library

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import node.buffer.BufferEncoding
import node.fs.readFileSync
import kotlin.test.Test

class ResourceLoadingTest {

    @Test
    fun canLoadResource() = asyncSetup(object {
    }) exercise {
        val dirname = js("__dirname").unsafeCast<String>()
        readFileSync(path = "$dirname/com/zegreatrob/wrapper/wdio/testing/library/cool-file.txt", BufferEncoding.utf8)
    } verify { result ->
        result.assertIsEqualTo("Cool File Content")
    }
}
