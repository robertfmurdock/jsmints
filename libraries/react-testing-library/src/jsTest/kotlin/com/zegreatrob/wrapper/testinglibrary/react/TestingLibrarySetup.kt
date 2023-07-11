package com.zegreatrob.wrapper.testinglibrary.react

import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.wrapper.testinglibrary.react.external.reactTestingLibrary
import react.create

val testingLibrarySetup = asyncTestTemplate(sharedSetup = {
    reactTestingLibrary.render(TestComponent.create())
})
