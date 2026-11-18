package com.zegreatrob.jsmints.plugins

import com.zegreatrob.jsmints.plugins.wrapper.WrapperTestExtension

plugins {
    base
    id("com.google.devtools.ksp")
}

val wrapper = project.extensions.create<WrapperTestExtension>("wrapper")

project.addJsmintsDependency("wrapper-processor", jsKspConfigurations) { wrapper.includedBuild.get() }
