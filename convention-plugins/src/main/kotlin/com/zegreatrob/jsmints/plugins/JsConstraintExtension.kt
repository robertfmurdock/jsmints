package com.zegreatrob.jsmints.plugins

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import java.io.File

abstract class JsConstraintExtension(val project: Project) {

    private var packageContents: Provider<String>? = null
    private var lastContents: String? = null
    private var parsedJson: JsonNode = NullNode.instance

    @Input
    var json: File? = null
        set(value) {
            field = value
            packageContents = value?.let {
                project.providers.fileContents(project.layout.projectDirectory.file(it.absolutePath)).asText
            }
        }

    private fun metadata(): JsonNode {
        val contents = packageContents?.orNull
        if (contents != lastContents) {
            parsedJson = contents?.let { objectMapper.readTree(it) } ?: NullNode.instance
            lastContents = contents
        }
        return parsedJson
    }

    fun dependencies() = metadata().get("dependencies")?.dependencyEntries()
    fun devDependencies() = metadata().get("devDependencies")?.dependencyEntries()
    val exists get() = json != null
    private fun JsonNode.dependencyEntries(): Sequence<Pair<String, JsonNode>> = properties().asSequence().map { entry ->
        entry.key to entry.value
    }

    operator fun invoke(name: String): Dependency = dependencies()!!
        .first { (key, _) -> key == name }
        .let { project.dependencies.npm(name, it.second.asText()) }
}

private val objectMapper = ObjectMapper()
