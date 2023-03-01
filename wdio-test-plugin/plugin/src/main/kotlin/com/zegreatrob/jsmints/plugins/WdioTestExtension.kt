package com.zegreatrob.jsmints.plugins

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.property

abstract class WdioTestExtension(val project: Project, private val objectFactory: ObjectFactory) {

    @Input
    var includedBuild = objectFactory.property<Boolean>().convention(false)

    @Input
    @Optional
    var wdioConfigFile = objectFactory.fileProperty()

    @Input
    @Optional
    var htmlReporter = objectFactory.property<Boolean>().convention(true)

    @Input
    @Optional
    var useChrome = objectFactory.property<Boolean>().convention(true)
}
