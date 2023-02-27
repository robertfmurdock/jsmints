package com.zegreatrob.jsmints.plugins

import org.gradle.api.Project
import org.gradle.api.tasks.Input

abstract class WdioTestExtension(val project: Project) {

    @Input
    var includedBuild: Boolean = false
}
