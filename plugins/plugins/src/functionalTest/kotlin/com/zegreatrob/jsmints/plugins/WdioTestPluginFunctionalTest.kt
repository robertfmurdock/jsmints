package com.zegreatrob.jsmints.plugins

import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class WdioTestPluginFunctionalTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

    @Test
    fun `e2e subproject uses the root kotlin npm install task`() {
        testProjectDir.newFile("settings.gradle.kts").writeText(
            """
            rootProject.name = "wdio-test-fixture"
            include(":e2e")
            """.trimIndent(),
        )
        val e2eProjectDir = testProjectDir.newFolder("e2e")
        e2eProjectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("com.zegreatrob.jsmints.plugins.wdiotest")
            }

            kotlin {
                js {
                    nodejs()
                }
            }

            wdioTest {
                baseUrl.set("http://localhost")
            }

            configurations.named("jsE2eTestImplementation") {
                dependencies.clear()
            }
            configurations.named("runnerConfiguration") {
                dependencies.clear()
            }
            file("runner.zip").writeBytes(byteArrayOf())
            dependencies {
                add("runnerConfiguration", files("runner.zip"))
            }
            """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(":e2e:e2eRun", "--dry-run", "--no-configuration-cache")
            .build()

        assertTrue(result.output.contains(":kotlinNpmInstall SKIPPED"))
        assertTrue(result.output.contains(":e2e:e2eRun SKIPPED"))
    }
}
