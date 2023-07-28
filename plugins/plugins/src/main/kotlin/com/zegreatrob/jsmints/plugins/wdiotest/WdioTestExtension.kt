package com.zegreatrob.jsmints.plugins.wdiotest

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property

abstract class WdioTestExtension(val project: Project, objectFactory: ObjectFactory) {

    @Input
    val baseUrl = objectFactory.property<String>()

    @Input
    var includedBuild = objectFactory.property<Boolean>().convention(false)

    @Input
    var wdioConfigFile = objectFactory.fileProperty()

    @Input
    var htmlReporter = objectFactory.property<Boolean>().convention(true)

    @Input
    var timelineReporter = objectFactory.property<Boolean>().convention(false)

    @Input
    val allureReporter = objectFactory.property<Boolean>().convention(false)

    @Input
    val allureReportHint = objectFactory.property<String>().convention("")

    @Input
    var useChrome = objectFactory.property<Boolean>().convention(true)

    @Input
    var chromeBinary = objectFactory.property<String?>().convention(null)

    @Input
    var useHeadless = objectFactory.property<Boolean>().convention(true)

    @Input
    var screenshotsOnFailure = objectFactory.property<Boolean>().convention(true)
}
