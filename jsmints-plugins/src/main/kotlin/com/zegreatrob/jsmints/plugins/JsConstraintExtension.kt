package com.zegreatrob.jsmints.plugins

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.Input
import java.io.File

abstract class JsConstraintExtension(val project: Project) {

    @Input
    var json: File? = null

    fun dependencies() = json?.let(::loadPackageJson)?.get("dependencies")?.dependencyEntries()
    fun devDependencies() = json?.let(::loadPackageJson)?.get("devDependencies")?.dependencyEntries()
    val exists get() = json != null && json != NullNode.instance
    private fun JsonNode.dependencyEntries() = fields().asSequence().map { entry ->
        entry.key to entry.value
    }

    operator fun invoke(name: String): Dependency = dependencies()!!
        .first { (key, _) -> key == name }
        .let { project.dependencies.npm(name, it.second.asText()) }

}

fun loadPackageJson(file: File): JsonNode {
    return if (file.exists()) ObjectMapper().readTree(file) else NullNode.instance
}
