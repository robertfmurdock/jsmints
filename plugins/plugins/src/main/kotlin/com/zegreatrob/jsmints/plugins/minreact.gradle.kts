package com.zegreatrob.jsmints.plugins

import com.zegreatrob.jsmints.plugins.minreact.MinreactTestExtension

plugins {
    base
    id("com.google.devtools.ksp")
}

val minreact = project.extensions.create<MinreactTestExtension>("minreact")

project.addJsmintsDependency("minreact-processor", jsKspConfigurations) { minreact.includedBuild.get() }
project.addJsmintsDependency("minreact", setOf("implementation", "jsMainImplementation")) { minreact.includedBuild.get() }
