package com.zegreatrob.testmints

data class CompoundMintTestException(val exceptions: Map<String, Throwable>) : Exception(
    message(exceptions), exceptions.values.first()
)

private fun message(failure: Map<String, Throwable>) = "Multiple exceptions occured.\n" +
        failure.map { (describer, exception) ->
            "$describer was: ${exception.message}\n"
        }.joinToString()
