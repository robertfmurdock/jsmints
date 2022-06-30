package com.zegreatrob.jsmints.plugins

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.Project
import java.io.File

abstract class JsToolsExtension(val packageJson: PackageJson)

fun Project.loadPackageJson(): PackageJson {
    val packageJsonPath = "${projectDir.path}/package.json"
    val file = File(packageJsonPath)
    val json = if(file.exists()) ObjectMapper().readTree(file) else null
    return PackageJson(json)
}
