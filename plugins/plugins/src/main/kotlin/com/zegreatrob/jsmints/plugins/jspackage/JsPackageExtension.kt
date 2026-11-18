package com.zegreatrob.jsmints.plugins.jspackage

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import org.gradle.api.Project

abstract class JsPackageExtension(private val json: JsonNode) {
    fun dependencies() = json.get("dependencies")?.dependencyEntries()
    fun devDependencies() = json.get("devDependencies")?.dependencyEntries()
    val exists get() = json != NullNode.instance
    private fun JsonNode.dependencyEntries(): Sequence<Pair<String, JsonNode>> = properties().asSequence().map { entry ->
        entry.key to entry.value
    }
}

fun Project.loadPackageJson(): JsonNode {
    val contents = providers.fileContents(layout.projectDirectory.file("package.json")).asText.orNull
    return contents?.let { ObjectMapper().readTree(it) } ?: NullNode.instance
}
