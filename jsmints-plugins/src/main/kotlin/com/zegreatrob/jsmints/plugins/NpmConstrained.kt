package com.zegreatrob.jsmints.plugins

import org.gradle.api.artifacts.Dependency
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

fun KotlinDependencyHandler.npmConstrained(name: String): Dependency =
    (project.extensions.getByName("jsconstraint") as JsConstraintExtension)
        .dependencies()!!
        .first { (key, _) -> key == name }
        .let { npm(name, it.second.asText()) }
