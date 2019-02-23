import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.20.0"
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "com.jfrog.bintray")

    group = "com.zegreatrob.testmints"
    version = "0.1.1"

    extensions.configure(BintrayExtension::class.java) {
        user = System.getenv("BINTRAY_USER")
        key = System.getenv("BINTRAY_KEY")
        override = true

        pkg(closureOf<BintrayExtension.PackageConfig> {
            repo = "zegreatrob"
            name = "testmints"

            version(closureOf<BintrayExtension.VersionConfig> {
            })
        })
    }

    val publishing = extensions.findByType(PublishingExtension::class.java)!!

    tasks {
        val bintrayUpload by getting(BintrayUploadTask::class) {
            doFirst {
                val publications = publishing.publications
                        .filterNot {
                            it.name.contains("-test")
                        }
                        .map { it.name }

                publishing.publications.getByName<MavenPublication>("kotlinMultiplatform") {
                    groupId = "com.zegreatrob.testmints"
                    artifactId = project.name
                    version = "${project.version}"
                }

                publishing.publications.filterIsInstance(MavenPublication::class.java)
                        .map {
                            it.artifact(file("build/publications/${it.name}/module.json")) {
                                extension = "module"
                            }
                        }

                publishing.publications.filterIsInstance(MavenPublication::class.java)
                        .map { it.artifacts }
                        .flatten()
                        .forEach { println("${it.file}") }

                setPublications(*publications.toTypedArray())
            }

            dependsOn("publishToMavenLocal")
        }
    }
}