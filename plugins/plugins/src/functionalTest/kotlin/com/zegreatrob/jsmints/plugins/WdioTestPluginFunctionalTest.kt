package com.zegreatrob.jsmints.plugins

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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

    @Test
    fun `installs the runner without resolving its transitive dependencies`() {
        val runnerVersion = "test"
        val repository = testProjectDir.newFolder("repository")
        writeRunnerModule(repository, runnerVersion)
        writeRuntimeModule(repository)

        testProjectDir.newFile("settings.gradle.kts").writeText(
            """
            import org.gradle.api.initialization.resolve.RepositoriesMode

            rootProject.name = "wdio-test-fixture"
            include(":e2e")

            dependencyResolutionManagement {
                repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
                repositories {
                    maven { url = uri("${repository.toURI()}") }
                }
            }
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

            configurations.named("runnerConfiguration") {
                resolutionStrategy.eachDependency {
                    if (requested.group == "com.zegreatrob.jsmints" && requested.name == "wdiorunner") {
                        useVersion("$runnerVersion")
                    }
                }
            }
            """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(":e2e:installRunner", "--no-configuration-cache")
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":e2e:installRunner")?.outcome)
    }

    private fun writeRunnerModule(repository: File, version: String) {
        val moduleDirectory = repository.resolve("com/zegreatrob/jsmints/wdiorunner/$version")
        moduleDirectory.mkdirs()
        moduleDirectory.resolve("wdiorunner-$version.pom").writeText(
            """
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.zegreatrob.jsmints</groupId>
              <artifactId>wdiorunner</artifactId>
              <version>$version</version>
              <dependencies>
                <dependency>
                  <groupId>test.fixture</groupId>
                  <artifactId>runner-runtime</artifactId>
                  <version>1.0</version>
                </dependency>
              </dependencies>
            </project>
            """.trimIndent(),
        )
        writeZip(moduleDirectory.resolve("wdiorunner-$version-executable.jar"))
    }

    private fun writeRuntimeModule(repository: File) {
        val moduleDirectory = repository.resolve("test/fixture/runner-runtime/1.0")
        moduleDirectory.mkdirs()
        moduleDirectory.resolve("runner-runtime-1.0.pom").writeText(
            """
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>test.fixture</groupId>
              <artifactId>runner-runtime</artifactId>
              <version>1.0</version>
            </project>
            """.trimIndent(),
        )
        writeZip(moduleDirectory.resolve("runner-runtime-1.0.jar"))
    }

    private fun writeZip(destination: File) {
        ZipOutputStream(FileOutputStream(destination)).use { output ->
            output.putNextEntry(ZipEntry("jsmints-wdiorunner.js"))
            output.write("console.log('runner');".toByteArray())
            output.closeEntry()
        }
    }
}
