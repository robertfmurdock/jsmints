package com.zegreatrob.testmints

data class CompoundMintTestException(val failure: Throwable, val exception: Throwable) : Exception(
        message(failure, exception), exception
)

private fun message(failure: Throwable, exception: Throwable) = "Test failed and also threw exception during teardown.\n" +
        "Failure was: ${failure.message}\n" +
        "Teardown exception was: ${exception.message}"
