package com.zegreatrob.jsmints.plugins.jspackage

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import org.gradle.api.Project
import java.io.File

abstract class JsPackageExtension(private val json: JsonNode) {
    fun dependencies() = json.get("dependencies")?.dependencyEntries()
    fun devDependencies() = json.get("devDependencies")?.dependencyEntries()
    val exists get() = json != NullNode.instance
    private fun JsonNode.dependencyEntries() = fields().asSequence().map { entry ->
        entry.key to entry.value
    }
}

fun Project.loadPackageJson(): JsonNode {
    val packageJsonPath = "${projectDir.path}/package.json"
    val file = File(packageJsonPath)
    return if (file.exists()) ObjectMapper().readTree(file) else NullNode.instance
}
