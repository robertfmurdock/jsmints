package com.zegreatrob.jsmints.plugins.minreact

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property

abstract class MinreactTestExtension(val project: Project, objectFactory: ObjectFactory) {

    @Input
    var includedBuild = objectFactory.property<Boolean>().convention(false)
}
