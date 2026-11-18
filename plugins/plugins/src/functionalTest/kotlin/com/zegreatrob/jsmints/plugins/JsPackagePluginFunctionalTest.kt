package com.zegreatrob.jsmints.plugins

import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class JsPackagePluginFunctionalTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

    @Test
    fun `package metadata changes invalidate configured dependencies`() {
        testProjectDir.newFile("settings.gradle.kts").writeText("rootProject.name = \"package-metadata-fixture\"")
        val packageJson = testProjectDir.newFile("package.json")
        packageJson.writeText("""{"dependencies":{"fixture-lib":"1.0.0"}}""")
        testProjectDir.newFile("build.gradle.kts").writeText(
            """
            plugins {
                id("com.zegreatrob.jsmints.plugins.jspackage")
            }
            kotlin { js { nodejs() } }
            val selectedVersion = jspackage.dependencies()!!.first { it.first == "fixture-lib" }.second.asText()
            tasks.register("inspectPackage") {
                val version = selectedVersion
                inputs.property("selectedVersion", version)
                doLast { println("selectedVersion=" + version) }
            }
            """.trimIndent(),
        )
        val runner = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments("inspectPackage", "--configuration-cache")
        assertTrue(runner.build().output.contains("selectedVersion=1.0.0"))
        assertTrue(runner.build().output.contains("Configuration cache entry reused."))

        packageJson.writeText("""{"dependencies":{"fixture-lib":"2.0.0"}}""")

        assertTrue(runner.build().output.contains("selectedVersion=2.0.0"))
    }
}
