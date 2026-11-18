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

    @Test
    fun `changing from an environment override to its default reruns WDIO`() {
        val root = testProjectDir.root
        testProjectDir.newFile("settings.gradle.kts").writeText("rootProject.name = \"wdio-environment-fixture\"")
        val source = testProjectDir.newFile("PrintEnvironment.java")
        source.writeText(
            """
            class PrintEnvironment {
                public static void main(String[] args) {
                    System.out.print(System.getenv("REPORT_DIR"));
                }
            }
            """.trimIndent(),
        )
        testProjectDir.newFile("build.gradle.kts").writeText(
            """
            import com.zegreatrob.jsmints.plugins.WdioTest

            plugins {
                id("com.zegreatrob.jsmints.plugins.wdiotest") apply false
            }

            tasks.register<WdioTest>("e2eRun") {
                val javaBin = file(System.getProperty("java.home")).resolve("bin")
                nodeExecPath = listOf("java", "java.exe").map(javaBin::resolve).first { it.isFile }.absolutePath
                argumentList.set(listOf(file("PrintEnvironment.java").absolutePath))
                logOutputFile.set(layout.buildDirectory.file("environment.txt"))
                defaultEnvironment("REPORT_DIR", providers.provider { "default" })
                providers.gradleProperty("reportDirectory").orNull?.let {
                    environment("REPORT_DIR", it)
                }
            }
            """.trimIndent(),
        )

        val runner = GradleRunner.create().withProjectDir(root).withPluginClasspath()
        val report = root.resolve("build/environment.txt")
        val overridden = runner.withArguments("e2eRun", "--configuration-cache", "-PreportDirectory=override").build()
        assertEquals(TaskOutcome.SUCCESS, overridden.task(":e2eRun")?.outcome)
        assertEquals("override", report.readText())

        val default = runner.withArguments("e2eRun", "--configuration-cache").build()
        assertEquals(TaskOutcome.SUCCESS, default.task(":e2eRun")?.outcome)
        assertEquals("default", report.readText())

        val repeated = runner.build()
        assertEquals(TaskOutcome.UP_TO_DATE, repeated.task(":e2eRun")?.outcome)
        assertTrue(repeated.output.contains("Configuration cache entry reused."))
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
