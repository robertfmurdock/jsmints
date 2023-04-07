package com.zegreatrob.jsmints.plugins

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.environment

open class WdioTest : NodeExec() {

    @Option(option = "tests", description = "Allows test matcher to be specified")
    @Input
    @Optional
    var tests: String? = null

    override fun exec() {
        if (tests != null) {
            environment("FGREP" to tests)
        }
        super.exec()
    }
}
