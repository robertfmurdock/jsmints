package com.zegreatrob.jsmints.plugins.wrapper

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property

abstract class WrapperTestExtension(val project: Project, objectFactory: ObjectFactory) {

    @Input
    var includedBuild = objectFactory.property<Boolean>().convention(false)
}
