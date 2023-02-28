package com.zegreatrob.jsmints.plugins

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import java.io.File

abstract class WdioTestExtension(val project: Project) {

    @Input
    var includedBuild: Boolean = false

    @Input
    @Optional
    var wdioConfigFile: File? = null
}
