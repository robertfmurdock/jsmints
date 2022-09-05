package com.zegreatrob.jsmints.plugins

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import org.gradle.api.tasks.Input
import java.io.File

abstract class JsConstraintExtension {

    @Input
    var json: File? = null

    fun dependencies() = json?.let(::loadPackageJson)?.get("dependencies")?.dependencyEntries()
    fun devDependencies() = json?.let(::loadPackageJson)?.get("devDependencies")?.dependencyEntries()
    val exists get() = json != null && json != NullNode.instance
    private fun JsonNode.dependencyEntries() = fields().asSequence().map { entry ->
        entry.key to entry.value
    }
}

fun loadPackageJson(file: File): JsonNode {
    return if (file.exists()) ObjectMapper().readTree(file) else NullNode.instance
}
