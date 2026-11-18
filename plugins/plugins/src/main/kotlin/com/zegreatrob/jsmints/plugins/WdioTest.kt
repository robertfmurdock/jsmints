package com.zegreatrob.jsmints.plugins

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Results depend on the browser and the external application under test")
abstract class WdioTest : NodeExec() {

    @Option(option = "tests", description = "Allows test matcher to be specified")
    @Input
    @Optional
    var tests: String? = null

    @get:Internal
    abstract val environmentDefaults: MapProperty<String, String>

    @get:Input
    val wdioEnvironment: Map<String, String>
        get() = environmentDefaults.get().mapNotNull { (name, defaultValue) ->
            when (val current = environment[name]) {
                EnvironmentDefault.VALUE -> name to defaultValue
                null -> null
                else -> name to current.toString()
            }
        }.toMap()

    fun defaultEnvironment(name: String, value: Provider<String>) {
        environmentDefaults.put(name, value)
        environment(name, EnvironmentDefault.VALUE)
    }

    override fun exec() {
        environment(wdioEnvironment)
        if (tests != null) {
            environment(mapOf("FGREP" to tests))
        }
        super.exec()
    }

    // Reserve a key without resolving its provider so later ExecSpec overrides still win.
    private enum class EnvironmentDefault {
        VALUE,
    }
}
