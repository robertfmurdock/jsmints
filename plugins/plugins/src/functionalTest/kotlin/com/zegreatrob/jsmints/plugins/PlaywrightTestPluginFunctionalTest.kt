package com.zegreatrob.jsmints.plugins

import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PlaywrightTestPluginFunctionalTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

    @Test
    fun `check schedules Playwright for a project applying only its plugin`() {
        testProjectDir.newFile("settings.gradle.kts").writeText("rootProject.name = \"playwright-consumer\"")
        testProjectDir.newFile("build.gradle.kts").writeText(
            """
            plugins {
                id("com.zegreatrob.jsmints.plugins.playwrighttest")
            }

            repositories {
                mavenCentral()
            }
            """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments("check", "--dry-run", "--configuration-cache")
            .build()

        assertTrue(result.output, result.output.contains(":installPlaywrightBrowser SKIPPED"))
        assertTrue(result.output, result.output.contains(":jsNodePlaywrightTest SKIPPED"))
        assertTrue(result.output, result.output.contains(":playwrightRun SKIPPED"))
    }
}
