package com.zegreatrob.plugins

import com.fasterxml.jackson.databind.JsonNode

data class PackageJson(val json: JsonNode?) {
    fun dependencies() = json?.get("dependencies")?.dependencyEntries()
    fun devDependencies() = json?.get("devDependencies")?.dependencyEntries()
    private fun JsonNode.dependencyEntries() = fields().asSequence().map { entry ->
        entry.key to entry.value
    }
}
